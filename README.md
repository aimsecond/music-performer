# music-performer

This project is aiming to create a new way to make music -- using hand gestures.

---
## Week of June 3rd
### App 0.1 version finished
- In our 0.1 version, we added code that allow users to use hand gesture to play different piano sounds. The whole process worked with little lattency, which is pretty good, however it is not super accurate. The problem has two main causes:
	- Detection Part. Right now we are using the color-based mechanism to filter out the backgroud pixels. However this works not very good in practice: when the contrast between hand colar and environment color has no significant diffence, we will get wrong hand region. I believe we could dig into this method more deeper and get better result filtering result after tweak our filter boundaries. Another possible approch is to use yolo, this will recognize hand position in real-time. This method 
	- The current method only feed one label per guesture to the SVM model, and that's won't produce very good result if the training data is so limited. We decide to add more sample as training labels and feed the to the SVM, so it could get better results. The drawback is more complex model will definitely cost more computation time. 

## Week of May 20th

## Week of May 27th
### Code structure & UI settled down
- We have discussed serveral possible ways of implementating the core logic. Including how to utilize the openCV libarary to distinguish the hand from the environment, and how to classify the hand guestures. Here's our plan(Might be changed in the future):
	- 4 activities total: LoginActivity,performActivity,playActivity,playlistActivity.
	- 3 helper classes: handDetection, adapter, myCameraView
	- Use native code, (c++ code) to learn the hand guesture. Use SVM to do the classification.
	- Basic UI has already been designed and coded.

### Plan for next week
- Next week we will continue to develop the codes for Handgesture detection. We have developed codes for detecting hand and finding the contour of the hand to extract it from the image utilizing OpenCV methods. In addition, based on the  [this post](http://eaglesky.github.io/2015/12/26/HandGestureRecognition/), we have developed codes for identifying and recording key parameter of human hands. The next step for code developing is to develop the computational functions. The computational functions are used to compute and decide the position of the fingertips, which requires larger computational performance. We will develop this part, including svm model for training based on the fingertips location to predict the user's handgestrue with c++.

## Week of May 20th

### UI and structure design
- We have built the basic structure and UI for the app. Currently, some concerns still need to be discuss and modify, including the orientation when the user start to create a piece. To optimize hand gesture detection and prevent other elements, such as user's head, which may impact the detection precision, the potrait orientation is better. However, it is difficult to detect/include multiple hands in such orientation.
- The play list adopt recycler list view to show the videos that were recorded by the user.

### Hand gesture recognition
##### Method of detecting hand gesture
- The whole idea of detecting the hand gesture is to separate hands from the environment and convert the video stream information to a structural mathematical model. This could help us get better result in the classifying stage.
- The basic idea of extracting hand gestures from the incoming stream is based on [this post](http://eaglesky.github.io/2015/12/26/HandGestureRecognition/). It uses color-based segmentation to separate hands from the environment. This method requires two-step pre-sampling, once for the environment color and once for the hand color. Then it filters out the area that doesn't look familiar with the color of hand, and finally we get a binary image. Future classifiation job are all based on this binary image. We have already tested this idea, and it worked, not very accurate. Sometimes it just include part of environment as hand as well. We are trying to explore if there's any better segmentation methods.

##### Method of classifying hand gesture
- The post we just metioned uses SVM to do the classification job. It is fast and responsive, and we really appreciated that. However, there are still some flaws:
	- The oringinal idea was trying to extract the feature by calculating the number of concave hull points and their corresponding angle; but in our test, it would count knuckles as fingures sometimes. Our solution is to calculate the convex instead.
- Another great method is using [YOLO](https://docs.google.com/presentation/d/1kAa7NOamBt4calBU9iHgT8a86RRHz9Yz2oh4-GTdX6M/edit#slide=id.g150bad67fe_1_2) at the beginning. We would try our best to include YOLO in our project, if time permits.

### Method of triggering the sound
- Like many other instrument APPs (for example, [virtual piano](https://android.jlelse.eu/creating-a-virtual-piano-for-android-b6d3ac05d961) and [Soundpool sample project](https://www.faultinmycode.com/2018/05/using-android-soundpool-build-piano-app.html)), the mechanism of producing sound is very straightforward:
	- create a map of sound(using Android soundpool library or add individual .wav files as soundbank)
	- bind the trigger event with corresponding  sound (in our case, the result of gesture recognition)
	- keep looping until exit this activity
- A sound actually have many parameters to be configured: pitch, velocity, effect, timbre, etc. We only decide to support real-time pitch modification for now. After we done with all the tasks above, we might add real-time velocity changing by using the proximity sensor.



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
