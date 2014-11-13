package com.example.ding.touchforcattest;


import android.test.ActivityInstrumentationTestCase2;

import com.example.ding.touchforcat.MainTouch;

public class ApplicationTest extends ActivityInstrumentationTestCase2<MainTouch> {
    public ApplicationTest() {
        super(MainTouch.class);
    }


    @Override
    protected void setUp() throws Exception {

        super.setUp();
    }

    public void testAdd() {
        System.out.println("add");
        int x = 0;
        int y = 0;
        MainTouch instance = new MainTouch();
        int expResult = 0;
        int result = instance.add(x, y);
        assertEquals(expResult, result);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}