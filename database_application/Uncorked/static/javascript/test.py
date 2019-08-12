from flask import Flask, render_template, redirect, url_for,request, jsonify
app = Flask(__name__)

@app.route('/index/')
def interactive():
	return render_template('index.html')

@app.route('/background_process')
def background_process():
	try:
		lang = request.args.get('output', 0, type=int)
			return jsonify(result=lang+1)
	except Exception as e:
		return str(e)

def test(a):
    import mysql.connector
    from mysql.connector import Error
    from mysql.connector import errorcode
    b = a+1
    return(b)
