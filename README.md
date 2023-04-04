# A Blog Gateway 

## Static Sites like JoshLong.com 
how do we publish blogs to something thats a static site, like joshlong.com?

## Contentful
how do we use Contentful, which is a popular CMS powering some of the blogs i work with all the time?

Here's how you'd publish a blog, given a key: 

```shell
curl --include \
     --request POST \
     --header 'Authorization: Bearer $PERSONAL_ACCESS_TOKEN' \
     --header 'Content-Type: application/vnd.contentful.management.v1+json' \
     --header 'X-Contentful-Content-Type: blogPost' \
     --data-binary '{
       "fields": {
         "title": {
           "en-US": "Test Title"
         },
         "body": {
           "en-US": "Your blog body"
         },
         "author": {
           "en-US": {
              "sys": {
              "type": "Link",
              "linkType": "Entry",
              "id": "$AUTHOR_ID"
            }
          }
         },
         "slug": {
           "en-US": "title-that-is-hyphenated"
         },
         "category": {
           "en-US": "Your category (either News/Engineering/Releases)"
         }
       }
     }' \
     https://api.contentful.com/spaces/$SPACEID/environments/testing/entries/
``` 

This creates a draft and should give you a `2xx` response which contains the ID of the draft blog post. You can then use that ID to publish it:

```shell 
curl --include \
     --request PUT \
     --header 'Authorization: Bearer $YOUR_PERSONAL_ACCESS_TOKEN' \
     --header 'X-Contentful-Version: 1' \
     https://api.contentful.com/spaces/$SPACEID/environments/testing/entries/$ENTRY_ID/published
```

These commands work against the `test` environment. once its working, switch it to `master`. 

## What about Uberflip? does it even offer an API? 

It looks [like it does](https://help.uberflip.com/hc/en-us/articles/360019084031-Get-Your-Uberflip-API-Key-and-Secret-Account-ID-and-Hub-IDs).. 

## Images
How do we publish images? does contentful support images? where do we stash them for a static sight like joshlong.com, whose content lives on github? i can't exactly upload images there. Maybe we could have a strategy to upload them an S3 bucket or something if theyre larger than a certain size, otherwise stash 'em in the github repo? and could we offer compression during the publication? if were going to do all this with messaging, does that mean we're going to send a folder full of images as part of a rabbitmq message? do we need a smart client to bundle everything up into a `.zip` file and then unpack it on the consumer? is this sort of like the podcast publication pipeline? 