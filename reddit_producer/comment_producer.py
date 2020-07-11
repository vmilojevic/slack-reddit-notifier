import praw
import config

from kafka import KafkaProducer
from kafka.errors import KafkaTimeoutError
from json import dumps


def format_comment(comment):
    formatted_com = {
        'id': comment.id,
        'text': comment.body,
        'link': f'https://www.reddit.com{comment.permalink}',
        'subreddit': comment.subreddit.display_name,
        'type': 'comment'
    }

    return formatted_com


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
        value_serializer = lambda x: dumps(x).encode('utf-8')
    )

    subreddit = reddit.subreddit("all")

    print("Starting comment producer...")
    for comment in subreddit.stream.comments():
        try:
            com = format_comment(comment)
            message = producer.send(config.TOPIC_UNFILTERED, value=com)
        except KafkaTimeoutError as e:
            print("Exception occurred during comment send")
            pass
