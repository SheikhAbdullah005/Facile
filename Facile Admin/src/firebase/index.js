import firebase from 'firebase/app';
import 'firebase/storage'
// Initialize
var firebaseConfig = {
  apiKey: "AIzaSyBQnehxTDGd4NoCVQU0J9NQLZer4DgLoMo",
  authDomain: "facile-1a96a.firebaseapp.com",
  databaseURL: "https://facile-1a96a.firebaseio.com",
  projectId: "facile-1a96a",
  storageBucket: "facile-1a96a.appspot.com",
  messagingSenderId: "102326689952",
  appId: "1:102326689952:web:13916d5130178c7f1ce9c5",
  measurementId: "G-5SSLESDKGM"
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);
firebase.analytics();

const storage = firebase.storage();
export {
  storage, firebase as default
}

