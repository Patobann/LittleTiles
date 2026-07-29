package team.creative.littletiles.client.render;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ColorVertexBuilderTest {

    @Test
    public void multipliesFullIntensityByTint() {
        assertEquals(128, ColorVertexBuilder.multiply(255, 128));
    }

    @Test
    public void multipliesExistingShadeByTint() {
        assertEquals(50, ColorVertexBuilder.multiply(100, 128));
    }

    @Test
    public void zeroTintRemovesChannel() {
        assertEquals(0, ColorVertexBuilder.multiply(255, 0));
    }
}
