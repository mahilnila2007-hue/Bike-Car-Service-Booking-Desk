/**
 * firebase-init.js
 * Firebase Web SDK initialization.
 * Replace the firebaseConfig values with your actual Firebase project config.
 * These are PUBLIC (Web API) credentials — safe to include in frontend.
 * DO NOT put the service account JSON here.
 */

// Firebase Web SDK (v9 compat mode for easier usage)
const firebaseConfig = {
  apiKey: "AIzaSyAzkBvhu2o6josX6QnEbyjk1c2pz73R2aQ",
  authDomain: "neka-garage.firebaseapp.com",
  projectId: "neka-garage",
  storageBucket: "neka-garage.firebasestorage.app",
  messagingSenderId: "385698984802",
  appId: "1:385698984802:web:d24a598f8e2659574d723b",
  measurementId: "G-YEZSPLFGZD"
};

// Initialize Firebase
if (!firebase.apps || !firebase.apps.length) {
  firebase.initializeApp(firebaseConfig);
}

const auth = firebase.auth();

// Make globally available
window.garageAuth = auth;
window.garageFirebase = firebase;
