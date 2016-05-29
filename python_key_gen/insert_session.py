import copy
import psycopg2
from opentok import OpenTok

apiKey = _
apiSecret = ""

opentok = OpenTok(apiKey, apiSecret)


def insert_to_db(generated_array):
    conn = psycopg2.connect("dbname='videochat' user='postgres' host='localhost' password=''")

    cur = conn.cursor()

    for i in generated_array:
        query = "INSERT INTO rooms (session, much) VALUES (%s, %s)"
        data = (i, 0)
        cur.execute(query, data)

    conn.commit()
    conn.close()


session = opentok.create_session()

sessions_array = []

for i in xrange(50):
    session = opentok.create_session()
    sessions_array.append(copy.deepcopy(session.session_id))

insert_to_db(sessions_array)
