package com.example.ding.touchforcattest;

import android.test.ActivityInstrumentationTestCase2;

import com.example.ding.touchforcat.FingerTouch;

/**
 * Created by Ding on 11/13/2014.
 */
public class ApplicationTest2 extends ActivityInstrumentationTestCase2<FingerTouch> {

    public ApplicationTest2() {
        super(FingerTouch.class);
    }


    @Override
    protected void setUp() throws Exception {

        super.setUp();
    }

    public void testMultiply() {
        System.out.println("multiply");
        int x = 0;
        int y = 0;
        FingerTouch instance = new FingerTouch();
        int expResult = 0;
        int result = instance.multiply(x, y);
        assertEquals(expResult, result);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}