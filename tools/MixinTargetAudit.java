import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class MixinTargetAudit {
 static final Map<String,ClassNode> cache=new HashMap<>();
 static ClassNode node(String name)throws Exception {
  if(cache.containsKey(name))return cache.get(name);
  try(var in=MixinTargetAudit.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")){
   if(in==null){cache.put(name,null);return null;}
   var n=new ClassNode();new ClassReader(in).accept(n,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);cache.put(name,n);return n;
  }
 }
 static List<MethodNode> methods(ClassNode n,String selector)throws Exception{
  var matches=new ArrayList<MethodNode>();
  for(var m:n.methods)if(selector.equals(m.name)||selector.equals(m.name+m.desc))matches.add(m);
  if(matches.isEmpty()&&n.superName!=null&&!selector.startsWith("<")){
   var parent=node(n.superName);if(parent!=null)matches.addAll(methods(parent,selector));
  }
  return matches;
 }
 public static void main(String[] args)throws Exception{
  int found=0,missing=0,optional=0;var results=new ArrayList<String>();var seen=new HashSet<String>();
  for(String row:Files.readAllLines(Path.of(args[0]))){
   String[] p=row.split("\t",-1);var n=node(p[1]);
   if(n==null){optional++;results.add("UNAVAILABLE\t"+p[0]+"\t"+p[1]);continue;}
   var matches=methods(n,p[2]);
   if(matches.isEmpty()){missing++;results.add("MISSING_METHOD\t"+p[0]+"\t"+p[1]+"\t"+p[2]);}
   else {
    found++;results.add("FOUND\t"+p[0]+"\t"+p[2]);
    for(var m:matches)if(((m.access&Opcodes.ACC_STATIC)!=0)!=p[4].equals("1"))results.add("STATIC_MISMATCH\t"+p[0]+"\t"+p[2]);
    if(!p[3].isEmpty())for(var anchor:p[3].split("\\|")){
     boolean hit=false;
     for(var m:matches)for(var ins:m.instructions){
      if(ins instanceof MethodInsnNode call&&anchor.equals("L"+call.owner+";"+call.name+call.desc))hit=true;
      if(ins instanceof FieldInsnNode field&&anchor.equals("L"+field.owner+";"+field.name+":"+field.desc))hit=true;
     }
     if(!hit)results.add("MISSING_ANCHOR\t"+p[0]+"\t"+p[2]+"\t"+anchor);
    }
   }
  }
  int members=0;
  if(args.length>2)for(var row:Files.readAllLines(Path.of(args[2]))){
   var p=row.split("\\t",-1);var n=node(p[1]);if(n==null)continue;
   boolean foundMember=false,correctStatic=false,correctDescriptor=false;
   for(var current=n;current!=null;current=current.superName==null?null:node(current.superName)){
    if(p[3].equals("field")){for(var f:current.fields)if(f.name.equals(p[2])){foundMember=true;correctDescriptor|=f.desc.equals(p[5]);correctStatic|=f.desc.equals(p[5])&&((f.access&Opcodes.ACC_STATIC)!=0)==p[4].equals("1");}}
    else {for(var m:current.methods)if(m.name.equals(p[2])){foundMember=true;correctDescriptor|=m.desc.equals(p[5]);correctStatic|=m.desc.equals(p[5])&&(p[2].equals("<init>")||((m.access&Opcodes.ACC_STATIC)!=0)==p[4].equals("1"));}}
   }
   if(!foundMember)results.add("MISSING_MEMBER\t"+p[0]+"\t"+p[2]);
   else if(!correctDescriptor)results.add("MEMBER_DESCRIPTOR_MISMATCH\t"+p[0]+"\t"+p[2]+"\t"+p[5]);
   else if(!correctStatic)results.add("MEMBER_STATIC_MISMATCH\t"+p[0]+"\t"+p[2]);
   else members++;
  }
  System.out.println("Matched member names/descriptors/staticness: "+members);
  Files.write(Path.of(args[1]),results);
  for(var line:results)if(!line.startsWith("FOUND")&&seen.add(line))System.out.println(line);
  System.out.println("Matched selectors: "+found+"; missing: "+missing+"; unavailable target selectors: "+optional);
 }
}