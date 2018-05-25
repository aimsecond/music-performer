# music-performer

This project is aiming to create a new way to make music -- using hand gestures.

---

## Week of May 20th

### UI and structure design
- We have built the basic structure and UI for the app. Currently, some concerns still need to be discuss and modify, including the orientation when the user start to create a piece. To optimize hand gesture detection and prevent other elements, such as user's head, which may impact the detection precision, the potrait orientation is better. However, it is difficult to detect/include multiple hands in such orientation.
- The play list adopt recycler list view to show the videos that were recorded by the user.

### Method of detecting hand gesture

## Week of May 13th

### UI and structure design
![capture](https://user-images.githubusercontent.com/34120533/40213368-f5877718-5a09-11e8-98e7-d9c921d11d3d.JPG)
![capture1](https://user-images.githubusercontent.com/34120533/40213403-2535ac64-5a0a-11e8-8528-f8152190d992.JPG)
![capture2](https://user-images.githubusercontent.com/34120533/40213412-30681fae-5a0a-11e8-9316-2d5bebae6f0a.JPG)
![capture3](https://user-images.githubusercontent.com/34120533/40213418-38153890-5a0a-11e8-9e25-f7e61b50e134.JPG)
- The first one is our first page in this app. User can either begin to record a clip of music or play the clip that he/she made before.
- The second one shows the page when the user choose to create a new clip. User need to calibrate before recording a new clip. In addition, user can choose other potential modes that we may add after we implement the basic function.
- The third one shows the page when the user choose to play their old clips. User can search the specific name of the clip.
- The forth one is a simple media play activity when user choose a specific clip for playing.

### Method of detecting hand gesture
- We investigate some other examples about hand gesture tracking, and decide to try color based hand gesture tracking. The potential stages consist of presampling for hand color and background color, which is used to compute the threshold for the binary images. Each binary image from different colors will be merged together. After that, using OpenCV functions, we need to find the convex as well as contour points of the hand from the binary image and compute the fingertip locations, and finally results a feature vector.
- Reference: http://simena86.github.io/blog/2013/08/12/hand-tracking-and-recognition-with-opencv/

### Plan for next week
1. Implement the basic UI structure and Activities structrue for our app.
2. Continue to investigate the hand gesture detection and begin to implement it with OpenCV functions and develop necessary codes.
3. Test the audio sounds that we would like to add to our app, and try to build the software structure for this part before applying the actual hand gesture detection function.

## First Presentation on May 10th

### objective

- Deliver a Android app allowing user to perform music with virtual instruments using non-touch hand gesture
- Implement a hand gesture recognition function to detect/recognize user’s fingers and play related musical sound
- Users are able to create a piece of melody with their hands and record their works

### Project details

- Referenced API/Library include HandWave library, Gesture API with OpenCV, or some other API/Libraries based on machine learning
- Tentative hardware sensors include camera, GPS, proximity sensor, etc.

### Demonstration

- Demo: Live demo on a real phone + pre-recorded demo video
- Worst-case: All the functions will work but may not have a very high accuracy or could not respond quickly.
- Best-case: All the functions work well with accurate gesture recognition, ultra-fast response time. Some other add-ons would also be introduced including practice mode, challenge mode, etc. 

### response from the judges

- If use YOLO, we'd better put computation part to the cloud and make android app a client.
- mobile openCV library might be different with the pc version. Need more investigation on that.
