from flask import Flask, render_template, jsonify, url_for, request
import mysql.connector,json, collections, datetime
import urllib
from mysql.connector import Error
from mysql.connector import errorcode

app = Flask(__name__)

#
# Setup page navigation
#

@app.route('/')
def index():
	return render_template('index.html')

@app.route('/delete_user')
def delete_user():
	return render_template('delete_user.html')

@app.route('/login')
def login():
	return render_template('login.html')

@app.route('/new_user')
def new_user():
	return render_template('new_user.html')

@app.route('/review')
def review():
	return render_template('review.html')

@app.route('/search')
def search():
	return render_template('search.html')

@app.route('/update_user')
def update_user():
	return render_template('update_user.html')

@app.route('/user_home')
def user_home():
	return render_template('user_home.html')

@app.route('/view_review')
def view_review():
	return render_template('view_review.html')

@app.route('/wine_profile')
def wine_profile():
	return render_template('wine_profile.html')

@app.route('/winery_home')
def winery_home():
	return render_template('winery_home.html')

@app.route('/variety_home')
def variety_home():
	return render_template('variety_home.html')
#
# background processes
#
@app.route('/login_receiver', methods = ['POST'])
# return all user info (username, uname, email) by username
def getUserInfo_login_func5():
	usersName =  '\'' + request.form['username'] + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getUserInfo(" + usersName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['username'] = row[0]
			d['uname'] = row[1]
			d['email'] = row[2]
			d['password'] = row[3]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print("usersName: " + usersName)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/new_user_receiver', methods = ['POST'])
def addUser_func11():
	usersName = '\'' + request.form['username'] + '\'';
	realName = '\'' + request.form['name'] + '\'';
	userPassword = '\'' + request.form['password'] + '\'';
	userEmail = '\'' + request.form['email'] + '\'';
	print(usersName + ' ' + realName + ' ' + userEmail + ' ' + userPassword)

	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

		cursor = mySQLConnection.cursor()
		query ="insert into user(username, uname, password, email)        values("+usersName+","+realName+","+userPassword+","+userEmail+ ")"
		cursor.execute(query)
		mySQLConnection.commit()
		print(usersName + ' ' + realName + ' ' + userEmail + ' ' + userPassword)

		# Stored Procedure Call Statement
		sqlExecSP="call getUserInfo(" + usersName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['username'] = row[0]
			d['uname'] = row[1]
			d['email'] = row[2]
			objects_list.append(d)

		j = '[{ "status":"OK" }]'
		print(j)
		return (j);

	except mysql.connector.Error as error:
		print("Duplicate username, please try a new one")
		j = '[{ "status":"Selected username already exists. Please try a new one" }]'
		return (j);
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/update_user_receiver/<usersName>', methods = ['POST'])
# update user in the User table
def updateUser_func11(usersName):
	usersName =  '\'' + usersName + '\'';
	realName =  '\'' + request.form['uname'] + '\'';
	userEmail = '\'' + request.form['email'] + '\'';
	userPassword = '\'' + request.form['password'] + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

		cursor = mySQLConnection.cursor()
		query ="update User set uname ="+realName+", password ="+userPassword+", email ="+userEmail+" where username ="+usersName
		cursor.execute(query)
		mySQLConnection.commit()
		print(usersName + " has been updated")
		return json.dumps({'status':'OK'});
	except mysql.connector.Error as error:
		print("Failed to execute query: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")def update_user_receiver(username):

# add new review to the reivew table
@app.route('/review_receiver/<usersName>/<wineID>', methods = ['POST'])
def addRivew_func14(usersName, wineID):
	usersName = '\'' + usersName + '\'';
	wineID = '\'' + wineID + '\'';
	reviewStar =  '\'' + request.form['rating'] + '\'';
	reviewComment = '\'' + request.form['comments'].replace('\'', '\'\'') + '\'';
	reviewFavlist = '\'' + request.form['favorite'] + '\'';
	reviewDate = '\'' + datetime.datetime.now().strftime("%x") + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

		cursor = mySQLConnection.cursor()
		query ="insert into Review(username, wid, date, comment, star, favlist)        values("+usersName+","+wineID+","+reviewDate+","+reviewComment+","+reviewStar+","+reviewFavlist+")"
		cursor.execute(query)
		mySQLConnection.commit()
		j = '[{ "status":"OK" }]'
		print(j)
		return (j);
	except mysql.connector.Error as error:
		print("Fail")
		j = '[{ "status":"You have already reviewed this wine. Please search for a new one to try." }]'
		return (j);
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/delete_user_receiver/<usersName>')
def deleteUser_func12(usersName):
	usersName =  '\'' + usersName + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

		cursor = mySQLConnection.cursor()
		query ="delete from User where username =" + usersName
		cursor.execute(query)
		mySQLConnection.commit()
		print(usersName + " has been deleted")
		return json.dumps({'status':'OK'});
	except mysql.connector.Error as error:
		print("Failed to execute query: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/user_home_receiver/<usersName>')
# return all user info (username, uname, email) by username
def getUserInfo_func5(usersName):
    usersName =  '\'' + usersName + '\'';
    try:
        mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
        sqlExecSP="call getUserInfo(" + usersName + ")"

        cursor = mySQLConnection.cursor(prepared=True)

        cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
        record=cursor.fetchall()

        objects_list = []
        for row in record:
			d = collections.OrderedDict()
			d['username'] = row[0]
			d['uname'] = row[1]
			d['email'] = row[2]
			objects_list.append(d)

        j = json.dumps(objects_list, ensure_ascii=False)
        print(j)
	return(j)
    except mysql.connector.Error as error:
        print("Failed to execute stored procedure: {}".format(error))
    finally:
        # closing database connection.
        if (mySQLConnection.is_connected()):
            cursor.close()
            mySQLConnection.close()
           # print("connection is closed")

@app.route('/user_home_receiver/counts/<usersName>')
# return all user info (username, uname, email) by username
def getUserInfo_count_func5(usersName):
    usersName =  '\'' + usersName + '\'';
    try:
        mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
        sqlExecSP="call getUserInfoWithCount(" + usersName + ")"

        cursor = mySQLConnection.cursor(prepared=True)

        cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
        record=cursor.fetchall()

        objects_list = []
        for row in record:
			d = collections.OrderedDict()
			d['username'] = row[0]
			d['uname'] = row[1]
			d['email'] = row[2]
			d['rcount'] = row[3]
			d['fcount'] = row[4]
			objects_list.append(d)

        j = json.dumps(objects_list, ensure_ascii=False)
        print(j)
	return(j)
    except mysql.connector.Error as error:
        print("Failed to execute stored procedure: {}".format(error))
    finally:
        # closing database connection.
        if (mySQLConnection.is_connected()):
            cursor.close()
            mySQLConnection.close()
           # print("connection is closed")

@app.route('/user_home_receiver/reviews/<usersName>')
# return list of reviews by username
def getUserReview_func6(usersName):
	usersName =  '\'' + usersName + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getUserReview(" + usersName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['username'] = row[0]
			d['wname'] = row[1]
			d['comment'] = row[2]
			d['star'] = row[3]
			d['price'] = row[4]
			d['score'] = row[5]
			d['yname'] = row[6]
			d['vname'] = row[7]
			d['wid'] = row[8]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/user_home_receiver/favorites/<usersName>')
def getFavWine_func8(usersName):
	usersName =  '\'' + usersName + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getFavWine(" + usersName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wname'] = row[1]
			d['yname'] = row[2]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/variety_home_receiver/<varietyName>')
def getVarietyWines(varietyName):
	varietyName =  '\'' + varietyName + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineNameV(" + varietyName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wname'] = row[1]
			d['yname'] = row[2]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/view_review_receiver/<usersName>/<wineID>')
def getReviewInfo(usersName, wineID):
	usersName =  '\'' + usersName + '\'';
	wineID =  '\'' + wineID + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getReviewInfo(" + usersName + "," + wineID + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['username'] = row[1]
			d['wname'] = row[2]
			d['comment'] = row[3]
			d['star'] = row[4]
			d['price'] = row[5]
			d['score'] = row[6]
			d['yname'] = row[7]
			d['vname'] = row[8]
			d['favorite'] = row[9]
			d['date'] = row[10]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/winery_home_receiver/<wineryName>')
# return all the wine name of a winery
def getWineryHomeInfo(wineryName):
	wineryName =  urllib.unquote(wineryName);
	wineryName =  '\'' + wineryName.replace('\'', '\'\'') + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineryInfo(" + wineryName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['yname'] = row[0]
			d['location'] = row[1]
			d['avg_score'] = row[2]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")


@app.route('/winery_home_receiver/more_wines/<wineryName>')
def winery_home_receiver_more_wines(wineryName):
	wineryName =  urllib.unquote(wineryName);
	wineryName =  '\'' + wineryName.replace('\'', '\'\'') + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineNameY(" + wineryName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wname'] = row[1]
			d['yname'] = row[2]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

# return wine ID, wine name, and winery name by matching characters
@app.route('/search_receiver/<wineChar>')
def getWine_func1(wineChar):
	wineChar =  '\'%' + wineChar + '%\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement

		sqlExecSP="call getWine(" + wineChar + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wname'] = row[1]
			d['yname'] = row[2]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

# return top 10 wines under $10
@app.route('/search_receiver/top_wines')
def getTop10Wines():
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement

		sqlExecSP="call getTopTenWinesUnder10()"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wname'] = row[1]
			d['yname'] = row[2]
			d['score'] = row[3]
			d['price'] = row[4]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/search_receiver/4_stars')
# return all wine names with average stars above x
def getWineAbove_func10():
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineAbove(4)"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wname'] = row[1]
			d['yname'] = row[2]
			d['price'] = row[3]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/wine_profile_receiver/<wineID>')
# return wine ID, wine name, description, price, expert score, variety and winery by wid
def getWineInfo_func2(wineID):
	wineID =  '\'' + wineID + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineInfo(" + wineID + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['wid'] = row[0]
			d['wdescription'] = row[1]
			d['score'] = row[2]
			d['price'] = row[3]
			d['wname'] = row[4]
			d['vname'] = row[5]
			d['yname'] = row[6]

			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/wine_profile_receiver/variety/<vName>')
# return variety info by vname
def getVarietyInfo(vName):
	vName =  '\'' + vName.replace('%20', ' ') + '\'';
	print(vName)
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getVarietyInfo(" + vName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['vname'] = row[0]
			d['vdescription'] = row[1]
			d['glass_description'] = row[2]
			d['glass_img'] = row[3]
			d['serving_temp'] = row[4]
			d['serving_info'] = row[5]

			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/wine_profile_receiver/winery/<wineryName>')
# return winery info by yname
def getWineryInfo(wineryName):
	wineryName =  urllib.unquote(wineryName);
	wineryName =  '\'' + wineryName.replace('\'', '\'\'') + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineryInfo(" + wineryName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['yname'] = row[0]
			d['location'] = row[1]

			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/user_home_receiver/avg_price/<usersName>')
# return the count of reviews and the average price of the wines that he reviewed by username
def getCountPrice_func7(usersName):
	usersName =  '\'' + usersName + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getCountPrice(" + usersName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['count_review'] = row[0]
			d['avg_price'] = row[1]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

# return top ten winerys with highest expert score on average in certain location
@app.route('/search_receiver/top_by_location/<locationName>')
def getWineryTopTen_func9(locationName):
	locationName =  '\'' + locationName + '\'';
	try:
		mySQLConnection = mysql.connector.connect(host='localhost',
                                          database='wine',
                                          user='root',
                                          password='chewman',
                                          use_pure = True)

        # Stored Procedure Call Statement
		sqlExecSP="call getWineryTopTen(" + locationName + ")"

		cursor = mySQLConnection.cursor(prepared=True)

		cursor.execute(sqlExecSP)

        # Fetch all rowset from execute
		record=cursor.fetchall()

		objects_list = []
		for row in record:
			d = collections.OrderedDict()
			d['yname'] = row[0]
			d['score'] = row[1]
			objects_list.append(d)

		j = json.dumps(objects_list, ensure_ascii=False)
		print(j)
		return(j)
	except mysql.connector.Error as error:
		print("Failed to execute stored procedure: {}".format(error))
	finally:
        # closing database connection.
		if (mySQLConnection.is_connected()):
			cursor.close()
			mySQLConnection.close()
           # print("connection is closed")

@app.route('/build_image_url/<filename>', methods=['POST'])
def build_image_url(filename):
	img_url = '<img src="../static/images/' + filename + '" />'
	print(img_url)
	return (img_url);

if __name__ == '__main__':
  app.run(host='0.0.0.0', port=80)
