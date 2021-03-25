# YonaVox
<img src="https://github.com/yonatankarimish/YonaVox/blob/master/misc/yonavox_image.jpg" alt="YonaVox screenshot" width="150"/>

This project hosts the source code for an Android speech recognition application using machine learning models to transcribe Hebrew speech into voice activation commands.
The app then links to an IoT sensor from a vendor called Sensibo (https://sensibo.com/products/sensibo-sky) based on credentials you enter once,
and controls your home air conditioner based on what you just said.

The model embedded in the app was trained on the voice of a single speaker (myself), so for me it works great :). However, there is no guarantee it will work for anyone else.
As far as the model is concerned, because it never listened to anyone else during training, you are all just speaking with a really weird accent and babbling nonsense. 
Meaning it might respong to your commands, but it might just not.

The models were trained using a Google Colaboratory environment on a Tesla P100 GPU.
You can access the training data, preprocessing pipeline and training code at the following link:
https://drive.google.com/drive/folders/1g0WQCYhRpJebZS8S1D4uE5_pdWj_wn9_ 

It is important to state this is not a production ready app in any way. 
Rather, is is meant as a proof of concept showing it is possible to train small models for home use.
Feel free to have a look at the code and build your own APK from the source, if you want to install it on your phone for whatever reason.

I will link the research paper behind the model and the medium article explaining the algorithms once they are both published.
