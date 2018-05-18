# music-performer

This project is aiming to create a new way to make music -- using hand gestures.

---
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


### Plan for next week

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
