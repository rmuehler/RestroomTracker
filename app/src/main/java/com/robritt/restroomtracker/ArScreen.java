package com.robritt.restroomtracker;

import android.support.v7.app.AppCompatActivity;

//
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.content.Context;
//import android.os.Build;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.widget.Toast;
//
//import com.google.ar.core.Anchor;
//import com.google.ar.core.HitResult;
//import com.google.ar.core.Plane;
//import com.google.ar.sceneform.AnchorNode;
//import com.google.ar.sceneform.ArSceneView;
//import com.google.ar.sceneform.rendering.ModelRenderable;
//import com.google.ar.sceneform.ux.ArFragment;
//import com.google.ar.sceneform.ux.BaseArFragment;
//import com.google.ar.sceneform.ux.TransformableNode;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
////import uk.co.appoly.arcorelocation.LocationScene;
//
public class ArScreen extends AppCompatActivity {
//    private static final String TAG = ArScreen.class.getSimpleName();
//    private static final double MIN_OPENGL_VERSION = 3.0;
//
//    private ArFragment arFragment;
//    private ModelRenderable andyRenderable;
//
//    @Override
//    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
//    // CompletableFuture requires api level 24
//    // FutureReturnValueIgnored is not valid
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (!checkIsSupportedDeviceOrFinish(this)) {
//            return;
//        }
//
//        setContentView(R.layout.activity_ar_screen);
//        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
//
//        // When you build a Renderable, Sceneform loads its resources in the background while returning
//        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
//        ModelRenderable.builder()
//                .setSource(this, R.raw.andy)
//                .build()
//                .thenAccept(new Consumer<ModelRenderable>() {
//                    @Override
//                    public void accept(ModelRenderable renderable) {
//                        andyRenderable = renderable;
//                    }
//                })
//                .exceptionally(
//                        new Function<Throwable, Void>() {
//                            @Override
//                            public Void apply(Throwable throwable) {
//                                Toast toast =
//                                        Toast.makeText(ArScreen.this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//                                toast.setGravity(Gravity.CENTER, 0, 0);
//                                toast.show();
//                                return null;
//                            }
//                        });
//
//        arFragment.setOnTapArPlaneListener(
//                new BaseArFragment.OnTapArPlaneListener() {
//                    @Override
//                    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//                        if (andyRenderable == null) {
//                            return;
//                        }
//
//                        // Create the Anchor.
//                        Anchor anchor = hitResult.createAnchor();
//                        AnchorNode anchorNode = new AnchorNode(anchor);
//                        anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                        // Create the transformable andy and add it to the anchor.
//                        TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//                        andy.setParent(anchorNode);
//                        andy.setRenderable(andyRenderable);
//                        andy.select();
//                    }
//                });
//    }
//
//    /**
//     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
//     * on this device.
//     *
//     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
//     *
//     * <p>Finishes the activity if Sceneform can not run
//     */
//    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            Log.e(TAG, "Sceneform requires Android N or later");
//            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
//            activity.finish();
//            return false;
//        }
//        String openGlVersionString =
//                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
//                        .getDeviceConfigurationInfo()
//                        .getGlEsVersion();
//        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
//            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
//            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
//                    .show();
//            activity.finish();
//            return false;
//        }
//        return true;
    }
//}