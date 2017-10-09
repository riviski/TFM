<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <script src="http://d3js.org/d3.v3.min.js"></script>
    <script src="d3.layout.cloud.js"></script>
    <script src="jquery-3.2.1.min.js"></script>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>beEmp project</title>

        <link href="css/bootstrap.min.css" rel="stylesheet">
        <link href="css/mdb.min.css" rel="stylesheet">
        <link href="css/style.css" rel="stylesheet">
        <link href="css/beEMP.css" rel="stylesheet">
    </head>

    <script>
        var color = d3.scale.linear()
            .domain([6, 0])
            .range(["#bbd254", "#689b31"]);

        var color_temp="";

        function createBarChart(){
            d3.csv("<%=request.getContextPath()+"/"+request.getParameter("filename")%>", function(data) {

                d3.select('body').select('section').select("#sectionRow").select("#sectionBarChart")
                    .selectAll('div.h-bar')
                    .data(data)
                    .enter()
                    .append('div')
                    .attr("class", 'h-bar')
                    .style('width', '10px')
                    .style('opacity', 0)
                    .on("mouseover", function(){
                        color_temp = d3.select(this).attr('style');
                        d3.select(this).transition().duration(300)
                            .style("background-color", "#333")
                            .style("color", "#fff")
                            .style("text-align", "right")
                            .text(function(d) { return "Similarity: "+d.similarity+"%"; });
                    })
                    .on("mouseout", function(){
                        d3.select(this).transition().duration(300)
                            .style('background-color', d3.rgb(color_temp))
                            .style("text-align", "left")
                            .text(function(d) { return d.filename; });
                        color_temp="";
                    })
                    .transition()
                    .delay(function(d, i) { return i * 1000 })
                    .duration(1000)
                    .style('width', function(d) { return d.similarity * 4 + 'px' })
                    .style('opacity', 1)
                    .style('background', function(d, i) { return color(i); })
                    .text(function(d) { return d.filename; });
            });
        }

        var tagsCloud = [];

        var width = 650;
        var height = 450;
        var color2 = d3.scale.category20();

        function wordCloud(){
            d3.csv("<%=request.getContextPath()+"/"+request.getParameter("filenameTags")%>", function(csv){
                csv.map(function(d){
                    var obj = {text: d.text, size: d.size};
                    tagsCloud.push(obj);
                });

                d3.layout.cloud()
                    .size([width, height])
                    .words(tagsCloud)
                    .rotate(function() {
                        return ~~(Math.random() * 2) * 90;
                    })
                    .font("Verdana")
                    .fontSize(function(d) {
                        return d.size*7;
                    })
                    .on("end", drawCloud)
                    .start();
            });
        }

        function drawCloud(tags) {
            d3.select('#sectionWordCloud').append('svg').attr('width', width).attr('height',height)
                .append("g")
                .attr("transform", "translate(" + (width / 3) + "," + (height / 3) + ")")
                .selectAll("text")
                .data(tags)
                .enter().append("text")
                .style("font-size", function(d) {
                    return d.size*6 + "px";
                })
                .style("font-family", "Verdana")
                .style("fill", function(d, i) {
                    return color2(i);
                })
                .attr("text-anchor", "middle")
                .attr("transform", function(d) {
                    return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
                })
                .text(function(d) {
                    return d.text;
                });
        }

        function init(){
            wordCloud();
            createBarChart();
        }
    </script>


    <body onload="init();">
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
                                    <h1>Stats dashboard</h1>
                                    <h4>Document: <%=request.getParameter("image")%></h4>
                                    <p>This dashboard displays a similarity ranking with the most similar documents.</br>On the right side a tags map is displayed showing the more relevant tags.</p>
                                </div>
                            </div>
                        </div>

                    </div>

                </div>
            </div>
        </main>
        <section class="container">
            <div id="sectionRow" class="col-md-12">
                <div id="sectionBarChart" class="col-md-6">
                </div>
                <div id="sectionWordCloud" class="col-md-6">
                </div>
            </div>
        </section>

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
