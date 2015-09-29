# -*- coding: utf-8 -*-
import random
import string
import sys

import sqlite3

def insert_to_db(generated_array):
	try:
        conn = sqlite3.connect('/Users/Nurislam/Downloads/chat_java_web/ChatDatabase')
    
    		cur = conn.cursor()    

    		for i in generated_array:
    			cur.execute("INSERT INTO keyGens(keyGen, isUse) VALUES (?, ?)", [i, 0])   
    			#print cur.fetchall() 

    		conn.commit()	          
    
	except sqlite3.Error, e:
		print "Error %s:" % e.args[0]
    		sys.exit(1)
        	print "error sqlite db"
	finally:
    		if conn:
        		conn.close()

def generate_fund(N):
	result = ""
	for i in xrange(4):
    		s = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(N)) 
    		result = result + s + "-"
	return result[0:-2]


generated_array = []

for i in xrange(100):	
    generated_array.append(generate_fund(4))


#print generated_array
#conn = sqlite3.connect('/Users/Nurislam/Downloads/chat_java_web/ChatDatabase.db')
    
#cur = conn.cursor()    

#for i in generated_array:
#    			cur.execute("INSERT INTO keyGens(keyGen, isUse) VALUES (" + i + ",0" + ");")     
insert_to_db(generated_array)









