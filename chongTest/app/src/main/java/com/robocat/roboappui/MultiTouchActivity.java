package com.robocat.roboappui;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.robocat.roboappui.commands.Control;
import com.robocat.roboappui.util.SystemUiHider;

import java.io.FileNotFoundException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * @author timgrannen
 * @see SystemUiHider
 */
public class MultiTouchActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    
    public static final String MULTITOUCH_SAVEFILE = "multitouch.rcc";
    
    private Control control = new Control();
    private int commandHistory;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi);
        TextView f = (TextView) findViewById(R.id.fullscreen_content);
        float size = 20;
        f.setTextSize(size);
        f.setText(R.string.MultiMainLabel);

        //final View controlsView = findViewById(R.id.fullscreen_content);
        final View contentView = findViewById(R.id.fullscreen_content);
        mActivePointers = new SparseArray<PointF>();
        histPointers = new SparseArray<PointF>();
        ListofTypes = new SparseArray<TypeOfAction>();

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    public void onVisibilityChange(boolean visible) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = contentView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            if(visible){
                                delayedHide(100);
                            }
//                            controlsView.animate()
//                                    .translationY(visible ? 0 : mControlsHeight)
//                                    .setDuration(mShortAnimTime);

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            //delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (TOGGLE_ON_CLICK) {
//                    mSystemUiHider.toggle();
//                } else {
//                    mSystemUiHider.hide();
//                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.fullscreen_content).setOnTouchListener(MultiTouchListener);
        //delayedHide(400);
        
        commandHistory = Control.newCommandHistory();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    @Override
    protected void onStop(){
        Control.saveCommandHistory(MULTITOUCH_SAVEFILE, this.getApplicationContext(), commandHistory);
        Control.closeCommandHistory(commandHistory);
        super.onStop();
    }

    public SparseArray<PointF> mActivePointers;
    public SparseArray<PointF> histPointers;
    public SparseArray<TypeOfAction> ListofTypes;
    public int ListCount = 0;


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener MultiTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            TextView Label=(TextView) findViewById(R.id.fullscreen_content);

            // get masked (not specific to a pointer) action
            int maskedAction = event.getActionMasked();

            TypeOfAction type;

            switch (maskedAction) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    ReBuildSparseArrays(event);
                    break;
                }

                case MotionEvent.ACTION_MOVE: { // a pointer was moved
                    type=Analyze();
                    if(type!=TypeOfAction.None){
                        ListofTypes.put(ListCount,type);
                        ListCount++;

                        Log.d("Analyze",type.toString());
                        //Label.setText(type.toString());
                    }

                    //Might not need this, idk
                    ReBuildSparseArrays(event);
                    break;

                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL: {
                    ReBuildSparseArrays(event);
                    if(event.getPointerCount()==1){
                        Final_and_set();
                        ListofTypes.clear();
                    }
                    break;
                }
            }
            return false;   //maybe return true, idk
        }

    };

    /**
     * Contains all of the possible actions on the multi-touch surface
     */
    public enum TypeOfAction{None,
        OneUp,OneDown,OneLeft,OneRight,OneDiagonal,
        TwoUp,TwoDown,TwoLeft,TwoRight,TwoDiagonal,TwoPinch,TwoExpand,
        ThreeUp,ThreeDown,ThreeLeft,ThreeRight};

    /**
     * Finds the TypeOfAction that per MotionEvent.
     * @return TypeOfAction - The result from the latest MotionEvent
     */
    private TypeOfAction Analyze(){
        switch (mActivePointers.size()){
            case 0:
                return TypeOfAction.None;
            case 1:
                return OneAnalyze(0);
            case 2:
                return TwoAnalyze();
            case 3:
                return ThreeAnalze();
            default:
                return TypeOfAction.None;
        }
    }

    /**
     *  Finds out what action was performed by an individual pointer
     * @param num The index of the pointer to be analyzed
     * @return The TypeOfAction of that one pointer
     */
    private TypeOfAction OneAnalyze(int num) {
        try{
            float x,y,histX,histY;

            x=mActivePointers.valueAt(num).x;
            y=mActivePointers.valueAt(num).y;
            histY=histPointers.valueAt(num).y;
            histX=histPointers.valueAt(num).x;

            float Xdiff=x-histX,Ydiff=y-histY;
            double ang=getAngle(Xdiff,Ydiff);


            return OneType(ang);
        }
        catch (Exception e){
            Log.d("Exeception One Analyze", "Got an execption");
        }
        return TypeOfAction.None;
    }

    /**
     * Finds the relative angle between two points with a difference in the x direction
     * (xdiff) and y direction (ydiff)
     * @param xdiff Distance apart in the x direction
     * @param ydiff Distance apart in the y direction
     * @return The closest double approximation of the arc tangent of y/x within the range [-pi..pi].
     */
    private double getAngle (float xdiff,float ydiff) {
        double angle=Math.atan2(ydiff,xdiff);
        Log.d("Angle", String.valueOf(angle));
        return angle;

    }

    /**
     *  Finds the TypeOfAction of a single event based on its angle
     * @param angle The angle found between two points with {@link #getAngle(float, float)}
     * @return A TypeOfAction representing where that angle was pointing
     */
    private TypeOfAction OneType(double angle){
        double  factor=20.0,
                lowVal=3.0/factor,
                highVal=7.0/factor,
                half=(factor/2.0)/factor,
                pi=Math.PI;

        if  ((angle>(-highVal*pi) && (-lowVal*pi)>angle )||
                (angle<(highVal*pi) && (lowVal*pi)<angle) ||
                (angle>(-(highVal+half)*pi) && (-(lowVal+half)*pi)>angle) ||
                (angle<((highVal+half)*pi) && ((lowVal+half)*pi)<angle)
                )
            return TypeOfAction.OneDiagonal;
        else{
            if(angle>(-lowVal*pi) && (lowVal*pi)>angle)
                return TypeOfAction.OneRight;   //was up
            else if(angle>(highVal*pi) && ((half+lowVal)*pi)>angle)
                return TypeOfAction.OneDown;   //was right
            else if(angle<(-highVal*pi) && (-(half+lowVal)*pi)<angle)
                return TypeOfAction.OneUp;   //was left
            else if(angle<-(half+highVal)*pi && angle>=-pi || angle>(half+highVal)*pi && angle<=pi)
                return TypeOfAction.OneLeft;   //was down

        }

        return TypeOfAction.None;
    }

    /**
     *  Finds out what kind of two pointer action has just happened by
     *  evaluating each pointer first and then handling all of the cases
     * @return The generalized TypeOfAction with two pointers
     */
    private TypeOfAction TwoAnalyze() {
        TypeOfAction oneT=OneAnalyze(0);
        TypeOfAction twoT=OneAnalyze(1);
        if(oneT==twoT){
            switch (oneT){
                case OneDown:
                    return TypeOfAction.TwoDown;
                case OneUp:
                    return TypeOfAction.TwoUp;
                case OneLeft:
                    return TypeOfAction.TwoLeft;
                case OneRight:
                    return TypeOfAction.TwoRight;
            }
        }
        else {

            if(oneT!=TypeOfAction.None && twoT!=TypeOfAction.None){
//                Log.d("Two Analyze Not Sim",oneT.toString()+"  "+twoT.toString());
                return TwoNotSimilar();
            }

        }
        return TypeOfAction.None;
    }

    /**
     * Defines a class of a PointF and an angle of movement
     */
    private class movingPoint{
        PointF p;
        double ang=0;
    }

    /**
     *  Returns the type of valid action the two pointers that are
     *  already known that they are going in the same direction
     * @return TypeOfAction if two pointers are not m
     */
    private TypeOfAction TwoNotSimilar() {
        movingPoint curOne=new movingPoint();
        movingPoint curTwo=new movingPoint();
        TypeOfAction temp=TypeOfAction.None;
        if(getPointsAndAngles(curOne,curTwo))
            temp= AnalyzeTwoPointsAndAngles(curOne,curTwo);

        Log.d("Two Not Sim","Returning: "+temp.toString());
        return temp;
    }

    /**
     *  Finds out if two movingPoints are being pinch together or
     *  expanded apart.
     * @param curOne First moving point
     * @param curTwo Second moving point
     * @return Either TwoPinch or TwoExpand
     */
    private TypeOfAction AnalyzeTwoPointsAndAngles(movingPoint curOne, movingPoint curTwo) {
        if(anglesConverge(curOne.ang,curTwo.ang)){
            //could be pinch or expand
            //check the points
            double  error=(4.0/20.0)*Math.PI;

            movingPoint topPoint;

            //true if a point is close to vertical
            if(curOne.ang<Math.PI/2+error && curOne.ang>Math.PI/2-error ||
                    curTwo.ang<Math.PI/2+error && curTwo.ang>Math.PI/2-error){

                if(curOne.p.y>curTwo.p.y){
                    topPoint=curOne;
                }
                else{
                    topPoint=curTwo;
                }
                //testing if topPoint is going away from the horizontal axis
                if(topPoint.ang<Math.PI && topPoint.ang>0){
                    return TypeOfAction.TwoExpand;
                }
                else{
                    return TypeOfAction.TwoPinch;
                }
            }
            else{
                //not close to vertical
                if(curOne.p.x>curTwo.p.x){
                    topPoint=curOne;
                }
                else{
                    topPoint=curTwo;
                }
                //testing if topPoint is going away from the vertical axis
                if(topPoint.ang<Math.PI/2 && topPoint.ang>(-Math.PI/2)){
                    return TypeOfAction.TwoExpand;
                }
                else{
                    return TypeOfAction.TwoPinch;
                }
            }
        }
        return TypeOfAction.None;
    }

    /**
     * Test to see if two angles are going away from or towards each other.
     * This is done by finding the difference between the angles and checking
     * to see if that is near PI or negative PI.
     *
     * <p>**This is not position dependent**
     * @param ang1
     * @param ang2
     * @return True if the angles are relatively the same line of movement, false otherwise.
     */
    public static boolean anglesConverge(double ang1, double ang2) {
        double error=(4.0/20.0)*Math.PI;
        double test=ang1-ang2;

        double  piPosError = Math.PI+error,
                piNegError = Math.PI-error;

        if( test>piNegError && test<piPosError ||
            test<-piNegError && test>-piPosError
          )
            return true;

        return false;
    }

    /**
     * Fills the two {@link com.robocat.roboappui.MultiTouchActivity.movingPoint}s with
     * their respective origins and angles of movement
     * @param curOne First moving point
     * @param curTwo Second moving point
     * @return True if both points were able to be filled with data, false otherwise.
     */
    private boolean getPointsAndAngles(movingPoint curOne,movingPoint curTwo) {
        try{
            float x,y,histX,histY;
            for(int num=0;num<mActivePointers.size();num++){
                x=mActivePointers.valueAt(num).x;
                y=mActivePointers.valueAt(num).y;
                histY=histPointers.valueAt(num).y;
                histX=histPointers.valueAt(num).x;

                float Xdiff=x-histX,Ydiff=y-histY;
                double ang=getAngle(Xdiff,Ydiff);
                if(num==0){
                    curOne.ang=ang;
                    curOne.p=new PointF(x,y);
                }
                else if(num==1){
                    curTwo.ang=ang;
                    curTwo.p=new PointF(x,y);
                    return true;
                }
                else{
                    Log.d("Getting Points and Angles","mActivePointers.size() > 2");
                }
            }

        }
        catch (Exception e){
            Log.d("Exeception getting Points and Angles", "Got an execption");
            return false;
        }
        return false;
    }

    /**
     *  Analyzes three pointers individually and returns a
     *  Three TypeOfAction if they all match and None otherwise.
     * @return The TypeOfAction related to the three pointer movement
     */
    private TypeOfAction ThreeAnalze() {
        TypeOfAction oneT=OneAnalyze(0);
        TypeOfAction twoT=OneAnalyze(1);
        TypeOfAction threeT=OneAnalyze(2);
        if(oneT==twoT && oneT==threeT){
            switch (oneT){
                case OneDown:
                    return TypeOfAction.ThreeDown;
                case OneUp:
                    return TypeOfAction.ThreeUp;
                case OneLeft:
                    return TypeOfAction.ThreeLeft;
                case OneRight:
                    return TypeOfAction.ThreeRight;
            }
        }
        else {
            Log.d("Three Analyze","Not a similar direction");
        }
        return TypeOfAction.None;
    }


    /**
     * Evaluates a built up list of TypeOfActions and displays a toast of the action that
     * had the highest number of entries in that list. This is to determine what the overall
     * action was of multiple MotionEvents within a single action (i.e. Two finger pinch
     * or three finger swipe down).
     */
    private void Final_and_set(){
        TextView Label=(TextView) findViewById(R.id.fullscreen_content);
        String Moved = "Final and Set ";
        //Label.setText(Moved);
        ListCount=0;
        TypeOfAction Final = TypeOfAction.None,temp;
        TypeOfAction [] actions= TypeOfAction.values();
        int [] numOfTypes = new int[actions.length];

        for(int i=0;i< actions.length;i++)
            numOfTypes[i]=0;

        for(int i=0;i<ListofTypes.size();i++){
            temp=ListofTypes.valueAt(i);
            for(int j=0;j<actions.length;j++){
                if(temp==actions[j])
                    numOfTypes[j]++;
            }
        }
        Final=getMaxNum(numOfTypes,actions);
        String s = Send_command(Final);
        if(!s.equals("None"))
            makeToast(s);
    }



    /**
     * Maps multi-touch events to commands and audio playback.
     * @return String to display in a toast after command is sent.
     */
    private String Send_command(TypeOfAction Final) {
        // map event to command
        String res = "None";

        String [] a = Control.getKnownCommands();
        Context context = getApplicationContext();
        String[] Audio_files;
        Audio_files = getResources().getStringArray(R.array.audio_files);
        try{
            switch (Final){
                case OneDown:
                case OneLeft:
                case OneUp:
                case OneRight:
                case TwoDown:
                case TwoUp:
                    res = Control.performTouchOperation(Final, context);
                    break;
                case OneDiagonal:
                    res = Control.performTouchOperation(Final, context);
                    if(res.equals("")){
                        //AudioChooser.Play_audio(context, Audio_files[0], Audio_files);
                        //res="Playing: " +Audio_files[0];
                    }
                    break;
                case TwoPinch:
                    res = Control.performTouchOperation(Final, context);
                    if(res.equals("")){
                        AudioChooser.Play_audio(context,Audio_files[1],Audio_files);
                        res="Playing: " +Audio_files[1];
                    }
                    break;
                case TwoExpand:
                    res = Control.performTouchOperation(Final, context);
                    if(res.equals("")){
                        AudioChooser.Play_audio(context,Audio_files[2],Audio_files);
                        res="Playing: " +Audio_files[2];
                    }
                    break;
                default:

                    break;
            }
        }
        catch (FileNotFoundException f){
            makeToast(f.getMessage());
            res="None";
        }
        return res;
    }








    /**
     *  This function gets the TypeOfAction that had the most entries within the
     *  acts array.
     * @param numOfTypes Array containing the counts of each TypeOfAction
     * @param acts  Array of TypeOfActions to be referenced to determine
     *              what TypeOfAction should be returned
     * @return The TypeOfAction with the highest amount of minor actions
     */
    private TypeOfAction getMaxNum(int[] numOfTypes,TypeOfAction[] acts) {
        int max=0;
        int maxIndex=0;
        for(int i=0;i<numOfTypes.length;i++){
            if(numOfTypes[i]>max){
                max=numOfTypes[i];
                maxIndex=i;
            }
        }
        return acts[maxIndex];
    }

    /**
     * Displays a toast with the value passed in s
     * @param s String value to display in the toast
     */
    public void makeToast(String s){
        Context context = getApplicationContext();
        Toast toast;
        if (!s.equals(""))
        	toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        else
        	toast = Toast.makeText(context, "No Action", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Rebuilds the arrays of current pointers and their historical points.
     * This is executed everytime a pointer is pressed onto or
     * released from the screen.
     * @param ev
     */
    private void ReBuildSparseArrays(MotionEvent ev){
        try{
            PointF OneHist,One;
            mActivePointers.clear();
            histPointers.clear();

            for(int i=0;i<ev.getPointerCount();i++){
                One = new PointF(ev.getX(i),ev.getY(i));
                int histpoint=0;
                OneHist = new PointF(ev.getHistoricalX(i,histpoint),ev.getHistoricalY(i,histpoint));
                histPointers.put(i,OneHist);
                mActivePointers.put(i,One);

            }
        }
        catch (Exception e){
            Log.d("Exeception", e.getMessage());
        }

    }

    /**
     *  Finds the distance between two given {@link android.graphics.PointF}s
     * @param one Point one
     * @param two Point two
     * @return The distance between two {@link android.graphics.PointF}s
     */
    private float distance(PointF one, PointF two) {
        float x = two.x-one.x,result=0;
        float y = two.y-two.y;
        x=x*x;
        y=y*y;
        result=FloatMath.sqrt(x+y);
        return result;
    }


    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable,delayMillis);
    }

    /**
     * Exits the current activity and returns to the previous one
     * @param v
     */
    public void Multi_Exit(View v){
        finish();
    }
}
