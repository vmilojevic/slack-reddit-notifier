import praw
import config

from kafka import KafkaProducer
from kafka.errors import KafkaTimeoutError
from json import dumps


def format_submission(submission):                                                        
    formatted_sub = {                                                           
        'id': submission.id,                                                                                                                       
        'title': submission.title,
        'text': submission.selftext,                                                 
        'link': f'https://www.reddit.com{submission.permalink}',
        'subreddit': submission.subreddit.display_name,                
        'type': 'submission'
    }

    return formatted_sub


if __name__ == "__main__":
    reddit = praw.Reddit(
        username = config.USERNAME,
        password = config.PASSWORD,
        client_id = config.CLIENT_ID,
        client_secret = config.CLIENT_SECRET,
        user_agent = config.USER_AGENT   
    )

    producer = KafkaProducer(
        bootstrap_servers = ['localhost:9092'],
        value_serializer = lambda x: dumps(x).encode('utf-8', 'ignore')
    )

    subreddit = reddit.subreddit("all")

    print("Starting submission producer...")
    for submission in subreddit.stream.submissions():
        try:
            if submission.is_self:
                sub = format_submission(submission)
                message = producer.send(config.TOPIC_UNFILTERED, value=sub)
        except KafkaTimeoutError as e:
            print("Exception occurred while sending submission")
            pass
