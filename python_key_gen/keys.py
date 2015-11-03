# -*- coding: utf-8 -*-
import random
import string
import sys
import psycopg2


def insert_to_db(generated_array, marker):

    conn = psycopg2.connect("dbname='videochat' user='postgres' host='localhost' password='ve;br'")

    cur = conn.cursor()

    for i in generated_array:
        query =  "INSERT INTO keyGens (keyGen, marker) VALUES (%s, %s);"
        data = (i, marker)

        cur.execute(query, data)

    conn.commit()
    conn.close()

def generate_fund():
    result = ""
    for i in xrange(4):
        s = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
        result = result + s + "-"
    return result[0:-1]


def insert_key():
    generated_array = []

    for i in xrange(50):
        generated_array.append(generate_fund())

    insert_to_db(generated_array, "sent")

    generated_array = []

    for i in xrange(250):
        generated_array.append(generate_fund())

    insert_to_db(generated_array, "not_used")

insert_key()