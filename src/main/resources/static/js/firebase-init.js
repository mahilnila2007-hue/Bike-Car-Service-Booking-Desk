/**
 * firebase-init.js
 * Firebase Web SDK initialization.
 * Replace the firebaseConfig values with your actual Firebase project config.
 * These are PUBLIC (Web API) credentials — safe to include in frontend.
 * DO NOT put the service account JSON here.
 */

// Firebase Web SDK (v9 compat mode for easier usage)
const firebaseConfig = {
  apiKey: window.FIREBASE_API_KEY || "YOUR_FIREBASE_WEB_API_KEY",
  authDomain: window.FIREBASE_AUTH_DOMAIN || "your-project.firebaseapp.com",
  projectId: window.FIREBASE_PROJECT_ID || "your-project-id",
  storageBucket: window.FIREBASE_STORAGE_BUCKET || "your-project.appspot.com",
  messagingSenderId: window.FIREBASE_MESSAGING_SENDER_ID || "123456789",
  appId: window.FIREBASE_APP_ID || "1:123456789:web:abcdef"
};

// Initialize Firebase
if (!firebase.apps || !firebase.apps.length) {
  firebase.initializeApp(firebaseConfig);
}

const auth = firebase.auth();

// Make globally available
window.garageAuth = auth;
window.garageFirebase = firebase;
