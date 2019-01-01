# InstagramDump
Dump Instagram pictures of public accounts

## What is this?
Download recent N days article's images of public Instagram accounts and write to MongoDB.

## How to run?

### Prerequisite
#### 1. Install Chrome
````bash
curl https://intoli.com/install-google-chrome.sh | bash
````

#### 2. Download chromedriver
* Download [here](https://chromedriver.storage.googleapis.com/index.html) with `LATEST_RELEASE` version and locate somewhere

#### 3. Set environment variables
````bash
# Chromedriver location
export CHROMEDRIVER_PATH='/home/occidere/apps/chromedriver'
# Directory that save images
export IMG_BASE_DIR='/var/img'
# MongoDB URI
export INSTAGRAMDUMP_MONGODB_URI='mongodb://127.0.0.1:27017/photo'
````

### Run it!
````bash
java -jar instagramdump.jar \
	 /job/instagramDumpJob.xml \
	 instagramDumpJob \
	 threadSize=1 \
	 dateRange=1 \
	 url=https://www.instagram.com/twicetagram/?hl=ko
````
* dateRange: Recent date range of articles to download images.
ex) if `dateRange` is 3 -> Download recent 3 days of article's images
* url: Instagram url to download images (ONLY Public accounts are avaliable!)

### Result
* Images will be downloaded at `IMG_BASE_DIR`
* Download result will be wrote in MongoDB
````json
{
	"_id" : "https://www.instagram.com/p/BsFjYRcgHHb/",
	"date" : "20190101182256",
	"author" : "twicetagram",
	"content" : "🤪🍓2019🍓🤪",
	"originImageUrls" : [
		"https://scontent-icn1-1.cdninstagram.com/vp/31839da1d63c24513388a1c9c91297de/5CCC26D2/t51.2885-15/e35/47694261_274316463242706_2264982466634087555_n.jpg?_nc_ht=scontent-icn1-1.cdninstagram.com",
		"https://scontent-icn1-1.cdninstagram.com/vp/75ba11c5faa1f52f7c6716dca149d626/5CD41EA5/t51.2885-15/e35/47581377_385349732009399_327281488380397826_n.jpg?_nc_ht=scontent-icn1-1.cdninstagram.com",
		"https://scontent-icn1-1.cdninstagram.com/vp/02491e51761eae2792fee82d828e32cd/5CB9E4FD/t51.2885-15/e35/47583731_948408112015758_8442928887998528277_n.jpg?_nc_ht=scontent-icn1-1.cdninstagram.com"
	],
	"savedImageUrls" : [
		"/var/img/45ab529b64d7cf5c8844afd567485952_twicetagram_20190101182256_00.jpg",
		"/var/img/dca59ceed3198d2d0ebd9982957d728a_twicetagram_20190101182256_01.jpg",
		"/var/img/dc25941f06dce8ca210a4e2551132acb_twicetagram_20190101182256_02.jpg"
	],
	"_class" : "org.occidere.instagramdump.domain.InstagramPhoto"
}
````

