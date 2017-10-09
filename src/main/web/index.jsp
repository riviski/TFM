<!DOCTYPE html>
<html lang="en">
    <script src="jquery-3.2.1.min.js"></script>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>beEmp project</title>
        <link rel="icon" type="image/png" href="favicon.png" />

        <link href="css/bootstrap.min.css" rel="stylesheet">
        <link href="css/mdb.min.css" rel="stylesheet">
        <link href="css/style.css" rel="stylesheet">
        <link href="css/beEMP.css" rel="stylesheet">

        <script>

            function checkFile(form, input){
                if(document.getElementById(input).value === ""){
                    alert("Please choose a file.");
                    return;
                }
                document.getElementById(form).submit();
            }

            function init(){
                <%if(request.getParameter("success") != null && request.getParameter("success").equalsIgnoreCase("true")){%>
                    $('#successHeader').fadeOut(5000);
                <%}else{%>
                    $('#successHeader').css('display', 'none');
                <%}%>
            }

        </script>
	</head>

	<body onload="init()">
        <header>
            <nav class="navbar navbar-dark primary-color-dark">

                <!-- Collapse button-->
                <button class="navbar-toggler hidden-sm-up" type="button" data-toggle="collapse" data-target="#collapseEx">
                    <i class="fa fa-bars"></i>
                </button>

                <div class="container">
                    <div class="collapse navbar-toggleable-xs" id="collapseEx">
                        <!--Links-->
                        <ul id="nav" class="nav navbar-nav">
                            <li class="nav-item active"><a href="http://localhost:8080/beEmp" class="waves-effect waves-light nav-link">Home</a></li>
                            <li class="nav-item"><a href="http://localhost:8080/beEmp/about.jsp" class="waves-effect waves-light nav-link">About</a></li>
                        </ul>
                    </div>
                </div>

            </nav>
        </header>

        <main>
            <!--Main layout-->
            <div class="container">
                <div class="row">

                    <!--Main column-->
                    <div class="col-md-12">

                        <div class="row">
                            <div class="col-md-12">

                                <div class="divider-new">
                                    <img src="logo.png" class="" alt="Logo" title="Logo">
                                </div>

                                <div class="site-index">
                                    <p id="successHeader" style="color: #aec453; display: inline;">Form submitted successfully</p>
                                    <!--First row-->
                                    <div class="row">

                                        <h2>Image Matching Web Service</h2>

                                        <!--Image matching columnn-->
                                        <form id="form1" action="/beEmp/api/image/match" method="POST" enctype="multipart/form-data">
                                            <div class="col-md-4">
                                                <!--Card-->
                                                <div class="card">
                                                    <!--Card image-->
                                                    <div class="view overlay hm-white-slight">
                                                        <img src="img/img-01.png" class="img-fluid" alt="">
                                                    </div>
                                                    <!--/.Card image-->

                                                    <!--Card content-->
                                                    <div class="card-block">
                                                        <!--Text-->
                                                        <input type="text" name="category" value="" placeholder="Category"/>
                                                        <input type="file" name="files" id="image1" style="font-size: 8pt;"/>
                                                        <a href="javascript:;" onclick="checkFile('form1','image1');" class="btn btn-primary center-block waves-effect waves-light">Matching image</a>
                                                    </div>
                                                    <!--/.Card content-->
                                                </div>
                                                <!--/.Card-->
                                            </div>
                                        </form>

                                        <form id="form2" action="/beEmp/api/image/insert" method="POST" enctype="multipart/form-data">
                                            <!--Insert image columnn-->
                                            <div class="col-md-4">
                                                <!--Card-->
                                                <div class="card">

                                                    <!--Card image-->
                                                    <div class="view overlay hm-white-slight">
                                                        <img src="img/img-subir.png" class="img-fluid" alt="">
                                                    </div>
                                                    <!--/.Card image-->

                                                    <!--Card content-->
                                                    <div class="card-block">
                                                        <!--Text-->
                                                        <input type="text" name="category" value="" placeholder="Category"/>
                                                        <input type="file" name="files" id="image2" style="font-size: 8pt;"/>
                                                        <a href="javascript:;" onclick="checkFile('form2','image2');" class="btn btn-primary center-block waves-effect waves-light">Insert image</a>
                                                    </div>
                                                    <!--/.Card content-->

                                                </div>
                                                <!--/.Card-->
                                            </div>
                                        </form>

                                        <form id="form3" action="/beEmp/api/image/delete" method="POST" enctype="application/x-www-form-urlencoded">
                                            <!--Delete image columnn-->
                                            <div class="col-md-4">
                                                <!--Card-->
                                                <div class="card">

                                                    <!--Card image-->
                                                    <div class="view overlay hm-white-slight">
                                                        <img src="img/img-borrar.png" class="img-fluid" alt="">
                                                    </div>
                                                    <!--/.Card image-->

                                                    <!--Card content-->
                                                    <div class="card-block">
                                                        <!--Text-->
                                                        <input type="text" name="filename" id="image3" value="" placeholder="Filename"/>
                                                        <a href="javascript:;" onclick="checkFile('form3','image3');" class="btn btn-primary center-block waves-effect waves-light">Delete image</a>
                                                    </div>
                                                    <!--/.Card content-->

                                                </div>
                                                <!--/.Card-->
                                            </div>
                                        </form>

                                    </div>
                                    <!--/.First row-->

                                    <br>
                                    <hr class="extra-margins">

                                    <!--Second row-->
                                    <div class="row">
                                        <h2>Document Matching Web Service</h2>

                                        <form id="form4" action="/beEmp/api/document/match" method="POST" enctype="multipart/form-data">
                                            <!-- Document matching columnn-->
                                            <div class="col-md-4">
                                                <!--Card-->
                                                <div class="card">

                                                    <!--Card image-->
                                                    <div class="view overlay hm-white-slight">
                                                        <img src="img/documentos.png" class="img-fluid" alt="">
                                                    </div>
                                                    <!--/.Card image-->

                                                    <!--Card content-->
                                                    <div class="card-block">
                                                        <!--Text-->
                                                        <input type="text" name="category" value="" placeholder="Category"/>
                                                        <input type="file" name="files" id="image4" style="font-size: 8pt;"/>
                                                        <a href="javascript:;" onclick="checkFile('form4','image4');" class="btn btn-primary center-block waves-effect waves-light">Matching document</a>
                                                    </div>
                                                    <!--/.Card content-->

                                                </div>
                                                <!--/.Card-->
                                            </div>
                                            <!--First columnn-->
                                        </form>

                                        <form id="form5" action="/beEmp/api/document/insert" method="POST" enctype="multipart/form-data">
                                            <!--Insert document columnn-->
                                            <div class="col-md-4">
                                                <!--Card-->
                                                <div class="card">

                                                    <!--Card image-->
                                                    <div class="view overlay hm-white-slight">
                                                        <img src="img/doc.subir.png" class="img-fluid" alt="">
                                                    </div>
                                                    <!--/.Card image-->

                                                    <!--Card content-->
                                                    <div class="card-block">
                                                        <!--Text-->
                                                        <input type="text" name="category" value="" placeholder="Category"/>
                                                        <input type="file" name="files" id="image5" style="font-size: 8pt;"/>
                                                        <a href="javascript:;" onclick="checkFile('form5','image5');" class="btn btn-primary center-block waves-effect waves-light">Insert document</a>
                                                    </div>
                                                    <!--/.Card content-->

                                                </div>
                                                <!--/.Card-->
                                            </div>
                                            <!--Second columnn-->
                                        </form>

                                        <form id="form6" action="/beEmp/api/document/delete" method="POST" enctype="application/x-www-form-urlencoded">
                                            <!--Delete document columnn-->
                                            <div class="col-md-4">
                                                <!--Card-->
                                                <div class="card">

                                                    <!--Card image-->
                                                    <div class="view overlay hm-white-slight">
                                                        <img src="img/doc.borrar.png" class="img-fluid" alt="">
                                                    </div>
                                                    <!--/.Card image-->

                                                    <!--Card content-->
                                                    <div class="card-block">
                                                        <!--Text-->
                                                        <input type="text" name="filename" id="image6" value="" placeholder="Filename"/>
                                                        <a href="javascript:;" onclick="checkFile('form6','image6');" class="btn btn-primary center-block waves-effect waves-light">Delete document</a>
                                                    </div>
                                                    <!--/.Card content-->

                                                </div>
                                                <!--/.Card-->
                                            </div>
                                            <!--Third columnn-->
                                        </form>
                                    </div>
                                    <!--/.Second row-->


                                </div>
                            </div>
                        </div>

                    </div>
                    <!--/.Main column-->

                </div>
            </div>
            <!--/.Main layout-->

        </main>

        <!--Footer-->
        <footer class="footer page-footer center-on-small-only primary-color-dark">
            <div class="footer-copyright">
                <div>
                    beEmp - 2017
                </div>
            </div>
        </footer>
	</body>
</html>