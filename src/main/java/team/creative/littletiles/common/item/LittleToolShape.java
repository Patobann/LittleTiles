package team.creative.littletiles.common.item;

import net.minecraft.item.ItemStack;

public enum LittleToolShape {

    BOX("Box"),
    CONNECTED("Connected"),
    SLICE("Slice"),
    INNER_CORNER("Inner corner"),
    OUTER_CORNER("Outer corner"),
    POLYGON("Polygon"),
    WALL("Wall"),
    PILLAR("Pillar"),
    CURVE("Curve"),
    CURVE_WALL("Curve wall"),
    CYLINDER("Cylinder"),
    SPHERE("Sphere"),
    PYRAMID("Pyramid");

    private static final String KEY = "shape";

    public final String title;

    LittleToolShape(String title) {
        this.title = title;
    }

    public static LittleToolShape get(ItemStack stack) {
        return byIndex(stack.hasTag() ? stack.getTag().getInt(KEY) : 0);
    }

    public static void set(ItemStack stack, LittleToolShape shape) {
        if (shape == BOX) {
            if (stack.hasTag())
                stack.getTag().remove(KEY);
        } else {
            stack.getOrCreateTag().putInt(KEY, shape.ordinal());
        }
    }

    public static LittleToolShape byIndex(int index) {
        LittleToolShape[] shapes = values();
        return shapes[Math.max(0, Math.min(index, shapes.length - 1))];
    }

    public static String[] titles() {
        LittleToolShape[] shapes = values();
        String[] titles = new String[shapes.length];
        for (int i = 0; i < shapes.length; i++)
            titles[i] = shapes[i].title;
        return titles;
    }
}
