package com.example.ding.touchforcattest;

import android.app.Application;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.TextView;

import com.example.ding.touchforcat.MainTouch;
import com.example.ding.touchforcat.FingerTouch;
import com.example.ding.touchforcat.R;

import junit.framework.Test;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2<MainTouch> {
    MainTouch activity;
    public ApplicationTest() {
        super(MainTouch.class);
    }


    @Override
    protected void setUp() throws Exception {

        super.setUp();
        activity = getActivity();
    }

    String message = "Hello World";
    MessageUtil messageUtil = new MessageUtil(message);



    @Test
    public void testPrintMessage() {
        assertEquals(message,messageUtil.printMessage());
    }
    @SmallTest
    public void test(){
     //   Button button=(Button) activity.findViewById(R.id.button1);
     //   assertNotNull(activity);

        assertNotNull(activity);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}