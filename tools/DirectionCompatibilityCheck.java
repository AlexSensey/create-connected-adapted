import com.hlysine.create_connected.ConnectedDirections;
import net.minecraft.core.Direction;

public class DirectionCompatibilityCheck {
    public static void main(String[] args) {
        int checked = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Direction result = ConnectedDirections.fromDelta(x, y, z);
                    boolean adjacent = Math.abs(x) + Math.abs(y) + Math.abs(z) == 1;
                    if ((result != null) != adjacent)
                        throw new AssertionError("Incorrect adjacency: " + x + "," + y + "," + z);
                    if (result != null && (result.getStepX() != x || result.getStepY() != y || result.getStepZ() != z))
                        throw new AssertionError("Incorrect face: " + result);
                    checked++;
                }
            }
        }
        System.out.println("Passed " + checked + " unit, zero, diagonal and distant-vector cases.");
    }
}
