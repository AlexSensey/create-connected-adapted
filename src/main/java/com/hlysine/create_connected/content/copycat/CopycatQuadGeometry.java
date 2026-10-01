package com.hlysine.create_connected.content.copycat;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Crops modern quad positions while interpolating their packed UV coordinates. */
public final class CopycatQuadGeometry {
    private CopycatQuadGeometry() {}

    public static BakedQuad cropAndMove(BakedQuad quad, AABB box, Vec3 move) {
        Vector3f edge1 = new Vector3f(quad.position1()).sub(quad.position0());
        Vector3f edge3 = new Vector3f(quad.position3()).sub(quad.position0());
        float aa = edge1.lengthSquared(), bb = edge3.lengthSquared(), ab = edge1.dot(edge3);
        float determinant = aa * bb - ab * ab;
        Vector3f[] positions = new Vector3f[4];
        long[] uvs = new long[4];
        for (int i = 0; i < 4; i++) {
            var original = quad.position(i);
            Vector3f clamped = new Vector3f(
                    (float) Math.max(box.minX, Math.min(box.maxX, original.x())),
                    (float) Math.max(box.minY, Math.min(box.maxY, original.y())),
                    (float) Math.max(box.minZ, Math.min(box.maxZ, original.z())));
            if (Math.abs(determinant) > 1e-12f) {
                Vector3f delta = new Vector3f(clamped).sub(quad.position0());
                float da = delta.dot(edge1), db = delta.dot(edge3);
                float along1 = (da * bb - db * ab) / determinant;
                float along3 = (db * aa - da * ab) / determinant;
                float u0 = u(quad.packedUV0()), v0 = v(quad.packedUV0());
                uvs[i] = pack(u0 + along1 * (u(quad.packedUV1()) - u0)
                                + along3 * (u(quad.packedUV3()) - u0),
                        v0 + along1 * (v(quad.packedUV1()) - v0)
                                + along3 * (v(quad.packedUV3()) - v0));
            } else {
                uvs[i] = quad.packedUV(i);
            }
            positions[i] = clamped.add((float) move.x, (float) move.y, (float) move.z);
        }
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3],
                uvs[0], uvs[1], uvs[2], uvs[3], quad.direction(), quad.materialInfo(),
                quad.bakedNormals(), quad.bakedColors());
    }

    private static float u(long uv) { return Float.intBitsToFloat((int) (uv >>> 32)); }
    private static float v(long uv) { return Float.intBitsToFloat((int) uv); }
    private static long pack(float u, float v) {
        return Integer.toUnsignedLong(Float.floatToIntBits(u)) << 32
                | Integer.toUnsignedLong(Float.floatToIntBits(v));
    }
}
