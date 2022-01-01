package com.adgad.kboard;


import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class KboardAccessibilityService extends AccessibilityService {

        private static String mWhatsappName;
        private AccessibilityNodeInfo titleNode;

        @Override
        public void onAccessibilityEvent(AccessibilityEvent event) {
                try {
                        mWhatsappName = "";
                        String packageName = event.getPackageName().toString();
                        if (packageName.equals("com.whatsapp")) {
                                titleNode = event.getSource().findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_name").get(0);
                        }
                        if(titleNode != null) {
                                mWhatsappName = titleNode.getText().toString();
                        }
                } catch(Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void onInterrupt() {
        }

        public static String getCurrentWhatsappName() {
                return mWhatsappName;
        }


}
