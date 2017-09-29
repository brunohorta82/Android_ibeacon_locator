package com.example.brunohorta.pixellocater;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void values() throws Exception {
        assertEquals(4, 2 + 2);
        final Point b1 = new Point(0, 0, "e20a39f4-73f5-4bc4-a12f-17d1ad07a961");
        final Point b2 = new Point(0, 9, "e20a39f4-73f5-4bc4-a12f-17d1ad07a962");
        final Point b3 = new Point(9, 0, "e20a39f4-73f5-4bc4-a12f-17d1ad07a963");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("b1x",b1.x);
            jsonObject.put("b1y",b1.y);
            jsonObject.put("b2x",b2.x);
            jsonObject.put("b2y",b2.y);
            jsonObject.put("b3x",b3.x);
            jsonObject.put("b3y",b3.y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jsonObject.toString());
    }
}