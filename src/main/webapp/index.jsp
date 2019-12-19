<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Content Server</title>
    </head>
    <body>
        <h1>Content Server</h1>
        <p>Provides REST interface to JSON documents</p>
        <h2>Endpoints:</h2>
        <table>
            <tr>
                <td>Operation</td>
                <td>Method</td>
                <td>URL</td>
            </tr>
            <tr>
                <td>Create document</td>
                <td>POST</td>
                <td><a href="rest/data">rest/data</a></td>
            </tr>
            <tr>
                <td>List documents</td>
                <td>GET</td>
                <td><a href="rest/data">rest/data</a></td>
            </tr>
            <tr>
                <td>Read document</td>
                <td>GET</td>
                <td><a href="rest/data/id">rest/data/{id}</a></td>
            </tr>
            <tr>
                <td>Replace document</td>
                <td>PUT</td>
                <td><a href="rest/data/id">rest/data/{id}</a></td>
            </tr>
            <tr>
                <td>Delete document</td>
                <td>DELETE</td>
                <td><a href="rest/data/id">rest/data/{id}</a></td>
            </tr>
        </table>

    </body>
</html>
