package com.example.ding.touchforcattest;

import android.test.ActivityInstrumentationTestCase2;

import com.example.ding.touchforcat.RecordTouch;

/**
 * Created by Ding on 11/13/2014.
 */
public class ApplicationTest3 extends ActivityInstrumentationTestCase2<RecordTouch> {

    public ApplicationTest3() {super(RecordTouch.class);}

    @Override
    protected void setUp() throws Exception {

        super.setUp();
    }

    public void testMinus() {
        System.out.println("minus");
        int x = 0;
        int y = 0;
        RecordTouch instance = new RecordTouch();
        int expResult = 0;
        int result = instance.minus(x, y);
        assertEquals(expResult, result);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
