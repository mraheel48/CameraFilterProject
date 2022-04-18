package com.example.deafultproject.filter;

import android.graphics.Color;
import android.opengl.GLES20;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.otaliastudios.cameraview.filter.BaseFilter;
import com.otaliastudios.cameraview.filter.TwoParameterFilter;


public class DuotoneFilterNew extends BaseFilter implements TwoParameterFilter {

    public DuotoneFilterNew() {
    }

    private final static String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            " varying highp vec2 vTextureCoord;\n" +
            " uniform " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + " sTexture;\n" +
            " uniform sampler2D lutTexture; // lookup texture\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     highp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
            "     textureColor = clamp(textureColor, 0.0, 1.0);\n" +

            "     highp float blueColor = textureColor.b * 63.0;\n" +

            "     highp vec2 quad1;\n" +
            "     quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +

            "     highp vec2 quad2;\n" +
            "     quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +

            "     highp vec2 texPos1;\n" +
            "     texPos1.x = clamp((quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n" +
            "     texPos1.y = clamp((quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n" +

            "     highp vec2 texPos2;\n" +
            "     texPos2.x = clamp((quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n" +
            "     texPos2.y = clamp((quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n" +

            "     highp vec4 newColor1 = texture2D(lutTexture, texPos1);\n" +
            "     highp vec4 newColor2 = texture2D(lutTexture, texPos2);\n" +

            "     gl_FragColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            " }";

    // Default values
    private int mFirstColor = Color.MAGENTA;
    private int mSecondColor = Color.YELLOW;
    private int mFirstColorLocation = -1;
    private int mSecondColorLocation = -1;


    /**
     * Sets the two duotone ARGB colors.
     *
     * @param firstColor  first
     * @param secondColor second
     */
    @SuppressWarnings({"unused"})
    public void setColors(@ColorInt int firstColor, @ColorInt int secondColor) {
        setFirstColor(firstColor);
        //setSecondColor(secondColor);
    }

    /**
     * Sets the first of the duotone ARGB colors.
     * Defaults to {@link Color#MAGENTA}.
     *
     * @param color first color
     */
    @SuppressWarnings("WeakerAccess")
    public void setFirstColor(@ColorInt int color) {
        // Remove any alpha.
        mFirstColor = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Sets the second of the duotone ARGB colors.
     * Defaults to {@link Color#YELLOW}.
     *
     * @param color second color
     */
    @SuppressWarnings("WeakerAccess")
    public void setSecondColor(@ColorInt int color) {
        // Remove any alpha.
        mSecondColor = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Returns the first color.
     *
     * @return first
     * @see #setFirstColor(int)
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    @ColorInt
    public int getFirstColor() {
        return mFirstColor;
    }

    /**
     * Returns the second color.
     *
     * @return second
     * @see #setSecondColor(int)
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    @ColorInt
    public int getSecondColor() {
        return mSecondColor;
    }

    @Override
    public void setParameter1(float value) {
        // no easy way to transform 0...1 into a color.
        setFirstColor((int) (value * 0xFFFFFF));
    }

    @Override
    public float getParameter1() {
        int color = getFirstColor();
        color = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
        return (float) color / 0xFFFFFF;
    }

    @Override
    public void setParameter2(float value) {
        // no easy way to transform 0...1 into a color.
        //setSecondColor((int) (value * 0xFFFFFF));
    }

    @Override
    public float getParameter2() {
        int color = getSecondColor();
        color = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
        return (float) color / 0xFFFFFF;
    }

    @NonNull
    @Override
    public String getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        mFirstColorLocation = GLES20.glGetUniformLocation(programHandle, "first");
        //  Egloo.checkGlProgramLocation(mFirstColorLocation, "first");
        mSecondColorLocation = GLES20.glGetUniformLocation(programHandle, "second");
        //Egloo.checkGlProgramLocation(mSecondColorLocation, "second");
    }

    @Override
    protected void onPreDraw(long timestampUs, @NonNull float[] transformMatrix) {
        super.onPreDraw(timestampUs, transformMatrix);
        float[] first = new float[]{
                Color.red(mFirstColor) / 255f,
                Color.green(mFirstColor) / 255f,
                Color.blue(mFirstColor) / 255f
        };
        float[] second = new float[]{
                Color.red(mSecondColor) / 255f,
                Color.green(mSecondColor) / 255f,
                Color.blue(mSecondColor) / 255f
        };
        GLES20.glUniform3fv(mFirstColorLocation, 1, first, 0);
        //Egloo.checkGlError("glUniform3fv");
        GLES20.glUniform3fv(mSecondColorLocation, 1, second, 0);
        // Egloo.checkGlError("glUniform3fv");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFirstColorLocation = -1;
        mSecondColorLocation = -1;
    }
}
