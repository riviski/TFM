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

    var width = 250;
    var height = 250;

    function position() {
        this.style("left", function(d,i) {
            if(i>11 && i<24){
                return (i-12)*20 + "px";
            }else if(i>23 && i<36){
                return (i-24)*20 + "px";
            }else if(i>35 && i<48){
                return (i-36)*20 + "px";
            }else if(i>47 && i<60){
                return (i-48)*20 + "px";
            }else if(i>59 && i<72){
                return (i-60)*20 + "px";
            }else if(i>71 && i<84){
                return (i-72)*20 + "px";
            }else if(i>83 && i<96){
                return (i-84)*20 + "px";
            }else if(i>95 && i<108){
                return (i-96)*20 + "px";
            }else if(i>107 && i<120){
                return (i-108)*20 + "px";
            }else if(i>119 && i<132){
                return (i-120)*20 + "px";
            }else if(i>131 && i<144){
                return (i-132)*20 + "px";
            }else {
                return i * 20 + "px";
            }
            })
            .style("top", function(d,i) {
                return Math.floor(i/12)*20 + "px";
            })
            .style("width", function(d) { return 20 + "px"; })
            .style("height", function(d) { return 20 + "px"; });
    }

    /* En vez de ser cuadrado el mapa, esta función genera un treeMap de diferentes tamaños

    function position() {
        this.style("left", function(d) { return d.x + "px"; })
            .style("top", function(d) { return d.y + "px"; })
            .style("width", function(d) { return Math.max(0, d.dx - 1) + "px"; })
            .style("height", function(d) { return Math.max(0, d.dy - 1) + "px"; });
    }*/

    function chooseColor(value){
        switch(value){
            case '0':
            case '2':
                return '#d6f2c3';
            case '1':
            case '3':
                return '#000000';
            default:
                return '#FFFFFF';
        }
    }

    function chooseBorderColor(value){
        switch(value){
            case '0':
            case '1':
                return 'solid 1px #FFFFFF';
            case '2':
            case '3':
                return 'solid 3px #FF0000';
            default:
                return 'solid 1px #FFFFFF';
        }
    }

    function drawMap(tree, div){
        var treemap = d3.layout.treemap()
            .size([width, height])
            .sticky(true)
            .value(function(d) { return d.size; });

        var node = div.datum(tree).selectAll(".node")
            .data(treemap.nodes)
            .enter().append("div")
            .attr("class", "node")
            .call(position)
            .style("background-color", function(d) {
                return d.name === 'tree' ? '#FFFFFF' : chooseColor(d.name); })
            .style("border", function(d) {
                return d.name === 'tree' ? 'solid 1px white' : chooseBorderColor(d.name); });
    }

    function getColors (i) {
        var colors = ['#aec453','#FFFFFF'];
        return colors[i];
    }

    function addDonutChart(similarity){
        var datos = {
            similitud: [similarity, 100-similarity]
        };
        var radio = width/2;
        var donut = d3.layout.pie().sort(null);

        var arc = d3.svg.arc()
            .innerRadius(radio-65)
            .outerRadius(radio-20);

        var svg = d3.select("#donutChart")
            .append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g")
            .attr("transform", "translate(" + width/2 + ","  + height/2  + ")");

        var path = svg.selectAll("path")
            .data(donut(datos.similitud))
            .enter().append("path")
            .attr("fill", function(d, i) { return getColors(i); })
            .transition()
            .duration(2000)
            .attrTween('d', function(d) {
                var i = d3.interpolate(d.startAngle+0.1, d.endAngle);
                return function(t) {
                    d.endAngle = i(t);
                    return arc(d)
                }
            });

        svg.append("text")
            .attr("dy", ".35em")
            .attr("font-size","40")
            .attr("style","font-family:Ubuntu")
            .attr("text-anchor", "middle")
            .attr("fill","#aec453")
            .text(similarity+"%");
    }

    function init(){
        var tree= {};
        var children = [];

        d3.csv("<%=request.getContextPath()+"/"+request.getParameter("filename1")%>", function(csv){
            csv.map(function(d){
                var obj = {name: d.hash, size: 1};
                children.push(obj);
            });

            tree["name"] = "tree";
            tree["children"] = children;

            var div = d3.select("body").select("#sectionImage1")
                .append("div")
                .style("position", "relative");

            drawMap(tree, div);
        });

        var tree2= {};
        var children2 = [];
        var similarity = <%=request.getParameter("similarity")%>;

        d3.csv("<%=request.getContextPath()+"/"+request.getParameter("filename2")%>", function(csv){
            csv.map(function(d){
                var obj = {name: d.hash, size: 1};
                children2.push(obj);
            });

            tree2["name"] = "tree";
            tree2["children"] = children2;

            var div2 = d3.select("body").select("#sectionImage2")
                .append("div")
                .style("position", "relative");

            drawMap(tree2, div2);

        });

        addDonutChart(similarity);
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

        <!--Main column-->
        <div class="col-md-12">

            <div class="row">
                <div class="col-md-12">

                    <div class="divider-new">
                        <img src="logo.png" class="" alt="Logo" title="Logo">
                    </div>

                    <div class="site-index">
                        <h1>Stats dashboard</h1>
                        <p>This dashboard displays a tree map showing the hash value for each image. Each green rectangle is a zero value, as each one value is a black rectangle.
                            The red areas are displaying the differences amongst the hashes.
                            </br>The donut chart displays the similarity percentage for both images.</p>
                    </div>
                </div>
            </div>

        </div>

    </div>
</main>

<section class="container">
    <div id="donutChart" class="col-md-6 col-md-offset-3" style="text-align: center">
        <h4>Similarity:</h4>
    </div>
    <div class="col-md-3">
    </div>

    <hr class="col-md-12">
    <div id="sectionRow" class="container" style="min-height: 650px">
        <div class="col-md-12">
            <div id="sectionImage1" class="col-md-4 col-md-offset-2">
                <h4><%=request.getParameter("originalImage")%></h4>
            </div>
            <div id="sectionImage2" class="col-md-4 col-md-offset-1">
                <h4><%=request.getParameter("comparedImage")%></h4>
            </div>
            <div class="col-md-3">
            </div>
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
