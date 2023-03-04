package com.adgad.kboard;


import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class KboardAccessibilityService extends AccessibilityService {

        private static String mWhatsappName;
        private static ArrayList<String> mWhatsappMessages = new ArrayList<String>();
        private AccessibilityNodeInfo titleNode;

        public static void logViewHierarchy(AccessibilityNodeInfo nodeInfo, final int depth) {

                if (nodeInfo == null) return;

                String spacerString = "";

                for (int i = 0; i < depth; ++i) {
                        spacerString += '-';
                }
                //Log the info you care about here... I choce classname and view resource name, because they are simple, but interesting.
                Log.d("TAG", spacerString + nodeInfo.getClassName() + " " + nodeInfo.getText());

                for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                        logViewHierarchy(nodeInfo.getChild(i), depth + 1);
                }
        }

        @Override
        public void onAccessibilityEvent(AccessibilityEvent event) {
                try {
                        mWhatsappName = "";
                        String packageName = event.getPackageName().toString();
                        AccessibilityNodeInfo root = getRootInActiveWindow();

                        if (event.getClassName().equals("com.whatsapp.Conversation") && root != null) {

                                mWhatsappMessages.clear();
                                List<AccessibilityNodeInfo> messages = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/message_text");

                                if(messages.size() > -1) {
                                        for (AccessibilityNodeInfo message : messages) {
                                                if (message.getParent().findAccessibilityNodeInfosByViewId("com.whatsapp:id/status").size() == 0) {
                                                        mWhatsappMessages.add("THEM: " + message.getText());
                                                        // it's the other person's message
                                                } else {
                                                        mWhatsappMessages.add("YOU: " + message.getText());
                                                }
                                        }
                                        titleNode = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_name").get(0);
                                }
                        }
                        if(titleNode != null) {
                                mWhatsappName = titleNode.getText().toString();
                        }
                } catch(Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        protected boolean onGesture(int gestureId) {
                return super.onGesture(gestureId);
        }

        @Override
        public void onInterrupt() {
        }

        public static String getCurrentWhatsappName() {
                return mWhatsappName;
        }
        public static ArrayList<String> getCurrentWhatsappMessages() {
                return mWhatsappMessages;
        }

}
