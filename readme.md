
**Backuper allows you to encrypt any data and hide them into images using steganography, and also get your data back.**

Project contains elements:

*config* - directory with configuration file for log4j

*lib* - external jars

*src* - source of application and unit tests. *src/magdysyuk/backuper/source* - application, *src/magdysyuk/backuper/test* - unit tests.

*unittests_files* - files used by unit tests. If you wouldn't run unit tests, you don't need this directory.

*build.xml* - ant script for build project.

##1. How to build it?##

You should have installed Java and Ant. Build script is placed at the root directory. In console get into this directory, and execute command 
>ant build

Or just use ready *backuper_v_1_0.jar* file from download section of github.

##2. How to use it?##

*2.1. How to hide data?*

Assume you are in the *build* directory in the console. You need to point some parameters. 
Path to data for processing is *C:\data\my_secret_data* (it could be a directory or a single file). 
Path to image you want to be a container for data is *D:\image.jpg* (you could use png or gif too). You want to get images contains data in *C:\data\images_contains_data* (could not exists), and report file *C:\data\report.xml*. Password for encryption will be *my_password*

In this case you should execute in the console:
>java -jar *backuper_v_1_0.jar* -action *hide* -password *my_password* -input-data *C:\data\my_secret_data* -image-source *D:\image.jpg* -report *C:\data\report.xml* -dir-images-with-data *C:\data\images_contains_data*

At the end you should see the text "Result of operation: SUCCESS". If you missed something, you will get a help message with description of all parameters.

*2.2. How to extract data?*

Now we want to get original data from these images back. 
Assume that we want get it to *C:\extracted_data* (directory could not exists), so we will get directory *C:\extracted_data\my_secret_data* with same content that directory *C:\data\my_secret_data* (or it will be just single file, depends of your data).

In this case you should execute in the console:
>java -jar *backuper_v_1_0.jar* -action *extract* -password *my_password* -dir-images-with-data *C:\data\images_contains_data* -report *C:\data\report.xml* -dir-output-data *C:\extracted_data*

Of course, you should use your own secret password and pathes to files.

##3. How it works?##
First, your data will be encrypted by AES 128 bit, CBC mode.

Next - encrypted data will be compressed to zip archive.

At the end, will be used LSB method for putting archive with encrypted data into image, where last 2 bits in each of colors of pixels will be replaced by data bits. Image will be resized, and if 1 image file is not enough for containing full data file, images will be copied.

For extracting mode all these steps are going in reverse order. From images and report xml file you will get a zip archive, which will be decompressed, and, at the end, files will be decrypted.

Simplified class diagram:
![Class diagram](http://i.minus.com/ibcKRrZhgX8Dl7.png)
 
