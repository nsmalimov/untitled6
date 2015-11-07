from opentok import OpenTok
import copy
import psycopg2

apiKey = 45400602; 
apiSecret = "e832beaf7185469a0f6c42ba4f2358d8c0e78165";

opentok = OpenTok(apiKey, apiSecret)

def insert_to_db(generated_array):

    conn = psycopg2.connect("dbname='videochat' user='postgres' host='localhost' password='ve;br'")
    
    cur = conn.cursor()    

    for i in generated_array:
        
        query =  "INSERT INTO rooms (session, much) VALUES (%s, %s);"
        data = (i, 0)
        #print data

        cur.execute(query, data)

    conn.commit()
    conn.close() 

session = opentok.create_session()

sessions_array = []
#sessions_array = [session.session_id]

#insert_to_db(sessions_array)

for i in xrange(50):
	session = opentok.create_session()
	sessions_array.append(copy.deepcopy(session.session_id))

insert_to_db(sessions_array)






