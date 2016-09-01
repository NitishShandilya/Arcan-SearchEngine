<?php
ini_set('memory_limit','-1');
// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');
include 'SpellCorrector.php';

$limit = 10;

$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;

$results = false;

if ($query)
{ 
  $queryArray = split(" ", $query);
  $correctedQueryArray = array();
  foreach($queryArray as $eachQuery)
  {
    $correctedEachQuery = SpellCorrector::correct($eachQuery);
    if(strcmp(strtolower($eachQuery), $correctedEachQuery) != 0 ){
      $correctedEachQuery = '<b><i>' . $correctedEachQuery . '</i></b>';
      array_push($correctedQueryArray, $correctedEachQuery);
    }
    else{
      array_push($correctedQueryArray, $eachQuery);
    }
  }
  $correctedQuery = join(" ", $correctedQueryArray);
  
  if(strcmp(strtolower($query), strtolower($correctedQuery)) != 0 )
  {
    $correctedRequestURI = $_SERVER['REQUEST_URI'];
    $correctedQueryToPrint = $correctedQuery;
    $correctedQuery = str_replace("<b><i>", "", $correctedQuery);
    $correctedQuery = str_replace("</i></b>", "", $correctedQuery);
    $correctedRequestURI = str_replace(urlencode($query), urlencode($correctedQuery), $correctedRequestURI);
    echo "Did you mean " . '<a href = '.$correctedRequestURI.'>' . $correctedQueryToPrint . '</a>'; 
  }
  else{
    echo $query;
  }
  
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample');

  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }

  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  try
  {
    if($_GET['pageRankType'] == "custom") {
      $additionalParameters = array('sort'=>'pageRankFile desc');
      $results = $solr->search($query, 0, $limit, $additionalParameters);
    } else if($_GET['pageRankType'] == "default"){
      $results = $solr->search($query, 0, $limit);  
    }    	  
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>
<html>
  <head>
    <title>PHP Solr Client Example</title>
    <!-- Ajax and jQuery -->
    <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <script src="//code.jquery.com/jquery-1.10.2.js"></script>
    <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
    
    <script src="stemmer.js"></script>
    
  </head>
  <body>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search:</label>
      <input type="text" name="q" id="q" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <br/>
      <input type="radio" name="pageRankType" value="default" <?php echo isset($_GET['pageRankType']) && $_GET['pageRankType'] == "custom" ? "" : "checked"; ?>/> Use Default Solr Page Rank 
      <input type="radio" name="pageRankType" value="custom" <?php echo isset($_GET['pageRankType']) && $_GET['pageRankType'] == "custom" ? "checked" : ""; ?>/> Use External Page Rank
      <br/>
      <input type="submit"/>
    </form>
<?php

// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
    <div>Total number of results <?php echo $total;?></div>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <ol>
<?php
  // iterate result documents
  foreach ($results->response->docs as $doc)
  {
?>
      <li>
        <table style="border: 1px solid black; text-align: left">
<?php
    
    $title = "N/A";
    $author = "N/A";
    $creation_date = "N/A";
    $stream_size = "N/A";
    $link = "";
      
    // iterate document fields / values
    foreach ($doc as $field => $value)
    {
      if ($field == "title"){
        $title = $value;
      } else if ($field == "author"){
        $author = $value;
      } else if ($field == "created"){
        $creation_date = $value;
      } else if ($field == "stream_size") {
        $stream_size = (string)round($value/1024.0,2);
      } else if ($field == "id") {
        $link = urldecode($value);
        //Convert to a clickable link
        $link = preg_replace('/\/home\/nitish\/shared\/crawl_data\//','',$link);
        $link = preg_replace('/\.html$/','',$link);
      }
    }
      
?>      
          <tr>
            <th><a href="<?php echo $link?>">Document</a></th>            
          </tr>
          <tr>
            <th>Title</th>
            <td><?php echo $title?></td>
          </tr>
          <tr>
            <th>Author</th>
            <td><?php echo $author?></td>
          </tr>
          <tr>
            <th>Date Created</th>
            <td><?php echo $creation_date?></td>
          </tr>
          <tr>
            <th>File Size(kB)</th>
            <td><?php echo $stream_size?></td>
          </tr>                 
</table>
      </li>
<?php
  }
?>
    </ol>
<?php
}
?>
    
    <!-- Autocomplete functionality -->
    <script>      
    $(document).ready(function(){            
      stopWords={'aapl:keywords' : '1', 'crawl_data' : '1', 'javascript' : '1', 'jquery': '1', 'larry.jpg' : '1', 'larry_at_desk.jpg' : '1', 'a' : '1' ,'about' : '1' ,'above' : '1' ,'across' : '1' ,'after' : '1' ,'again' : '1' ,'against' : '1' ,'all' : '1' ,'almost' : '1' ,'alone' : '1' ,'along' : '1' ,'already' : '1' ,'also' : '1' ,'although' : '1' ,'always' : '1' ,'among' : '1' ,'an' : '1' ,'and' : '1' ,'another' : '1' ,'any' : '1' ,'anybody' : '1' ,'anyone' : '1' ,'anything' : '1' ,'anywhere' : '1' ,'are' : '1' ,'area' : '1' ,'areas' : '1' ,'around' : '1' ,'as' : '1' ,'ask' : '1' ,'asked' : '1' ,'asking' : '1' ,'asks' : '1' ,'at' : '1' ,'away' : '1' ,'b' : '1' ,'back' : '1' ,'backed' : '1'  ,'backing' : '1' ,'backs' : '1' ,'be' : '1' ,'became' : '1' ,'because' : '1' ,'become' : '1' ,'becomes' : '1' ,'been' : '1' ,'before' : '1' ,'began' : '1' ,'behind' : '1' ,'being' : '1' ,'beings' : '1' ,'best' : '1' ,'better' : '1' ,'between' : '1' ,'big' : '1' ,'both' : '1' ,'but' : '1' ,'by' : '1' ,'c' : '1' ,'came' : '1' ,'can' : '1' ,'cannot' : '1' ,'case' : '1' ,'cases' : '1' ,'certain' : '1' ,'certainly' : '1' ,'clear' : '1' ,'clearly' : '1' ,'come' : '1' ,'could' : '1' ,'d' : '1' ,'did' : '1' ,'differ' : '1' ,'different' : '1' ,'differently' : '1' ,'do' : '1' ,'does' : '1' ,'done' : '1' ,'down' : '1' ,'down' : '1' ,'downed' : '1' ,'downing' : '1' ,'downs' : '1' ,'during' : '1' ,'e' : '1' ,'each' : '1' ,'early' : '1' ,'either' : '1' ,'end' : '1' ,'ended' : '1' ,'ending' : '1' ,'ends' : '1' ,'enough' : '1' ,'even' : '1' ,'evenly' : '1' ,'ever' : '1' ,'every' : '1' ,'everybody' : '1' ,'everyone' : '1' ,'everything' : '1' ,'everywhere' : '1' ,'f' : '1' ,'face' : '1' ,'faces' : '1' ,'fact' : '1' ,'facts' : '1' ,'far' : '1' ,'felt' : '1' ,'few' : '1' ,'find' : '1' ,'finds' : '1' ,'first' : '1' ,'for' : '1' ,'four' : '1' ,'from' : '1' ,'full' : '1' ,'fully' : '1' ,'further' : '1' ,'furthered' : '1' ,'furthering' : '1' ,'furthers' : '1' ,'g' : '1' ,'gave' : '1' ,'general' : '1' ,'generally' : '1' ,'get' : '1' ,'gets' : '1' ,'give' : '1' ,'given' : '1' ,'gives' : '1' ,'go' : '1' ,'going' : '1' ,'good' : '1' ,'goods' : '1' ,'got' : '1' ,'great' : '1' ,'greater' : '1' ,'greatest' : '1' ,'group' : '1' ,'grouped' : '1' ,'grouping' : '1' ,'groups' : '1' ,'h' : '1' ,'had' : '1' ,'has' : '1' ,'have' : '1' ,'having' : '1' ,'he' : '1' ,'her' : '1' ,'here' : '1' ,'herself' : '1' ,'high' : '1' ,'high' : '1' ,'high' : '1' ,'higher' : '1' ,'highest' : '1' ,'him' : '1' ,'himself' : '1' ,'his' : '1' ,'how' : '1' ,'however' : '1' ,'i' : '1' ,'if' : '1' ,'important' : '1' ,'in' : '1' ,'interest' : '1' ,'interested' : '1' ,'interesting' : '1' ,'interests' : '1' ,'into' : '1' ,'is' : '1' ,'it' : '1' ,'its' : '1' ,'itself' : '1' ,'j' : '1' ,'just' : '1' ,'k' : '1' ,'keep' : '1' ,'keeps' : '1' ,'kind' : '1' ,'knew' : '1' ,'know' : '1' ,'known' : '1' ,'knows' : '1' ,'l' : '1' ,'large' : '1' ,'largely' : '1' ,'last' : '1' ,'later' : '1' ,'latest' : '1' ,'least' : '1' ,'less' : '1' ,'let' : '1' ,'lets' : '1' ,'like' : '1' ,'likely' : '1' ,'long' : '1' ,'longer' : '1' ,'longest' : '1' ,'m' : '1' ,'made' : '1' ,'make' : '1' ,'making' : '1' ,'man' : '1' ,'many' : '1' ,'may' : '1' ,'me' : '1' ,'member' : '1' ,'members' : '1' ,'men' : '1' ,'might' : '1' ,'more' : '1' ,'most' : '1' ,'mostly' : '1' ,'mr' : '1' ,'mrs' : '1' ,'much' : '1' ,'must' : '1' ,'my' : '1' ,'myself' : '1' ,'n' : '1' ,'necessary' : '1' ,'need' : '1' ,'needed' : '1' ,'needing' : '1' ,'needs' : '1' ,'never' : '1' ,'new' : '1' ,'new' : '1' ,'newer' : '1' ,'newest' : '1' ,'next' : '1' ,'no' : '1' ,'nobody' : '1' ,'non' : '1' ,'noone' : '1' ,'not' : '1' ,'nothing' : '1' ,'now' : '1' ,'nowhere' : '1' ,'number' : '1' ,'numbers' : '1' ,'o' : '1' ,'of' : '1' ,'off' : '1' ,'often' : '1' ,'old' : '1' ,'older' : '1' ,'oldest' : '1' ,'on' : '1' ,'once' : '1' ,'one' : '1' ,'only' : '1' ,'open' : '1' ,'opened' : '1' ,'opening' : '1' ,'opens' : '1' ,'or' : '1' ,'order' : '1' ,'ordered' : '1' ,'ordering' : '1' ,'orders' : '1' ,'other' : '1' ,'others' : '1' ,'our' : '1' ,'out' : '1' ,'over' : '1' ,'p' : '1' ,'part' : '1' ,'parted' : '1' ,'parting' : '1' ,'parts' : '1' ,'per' : '1' ,'perhaps' : '1' ,'place' : '1' ,'places' : '1' ,'point' : '1' ,'pointed' : '1' ,'pointing' : '1' ,'points' : '1' ,'possible' : '1' ,'present' : '1' ,'presented' : '1' ,'presenting' : '1' ,'presents' : '1' ,'problem' : '1' ,'problems' : '1' ,'put' : '1' ,'puts' : '1' ,'q' : '1' ,'quite' : '1' ,'r' : '1' ,'rather' : '1' ,'really' : '1' ,'right' : '1' ,'right' : '1' ,'room' : '1' ,'rooms' : '1' ,'s' : '1' ,'said' : '1' ,'same' : '1' ,'saw' : '1' ,'say' : '1' ,'says' : '1' ,'second' : '1' ,'seconds' : '1' ,'see' : '1' ,'seem' : '1' ,'seemed' : '1' ,'seeming' : '1' ,'seems' : '1' ,'sees' : '1' ,'several' : '1' ,'shall' : '1' ,'she' : '1' ,'should' : '1' ,'show' : '1' ,'showed' : '1' ,'showing' : '1' ,'shows' : '1' ,'side' : '1' ,'sides' : '1' ,'since' : '1' ,'small' : '1' ,'smaller' : '1' ,'smallest' : '1' ,'so' : '1' ,'some' : '1' ,'somebody' : '1' ,'someone' : '1' ,'something' : '1' ,'somewhere' : '1' ,'state' : '1' ,'states' : '1' ,'still' : '1' ,'still' : '1' ,'such' : '1' ,'sure' : '1' ,'t' : '1' ,'take' : '1' ,'taken' : '1' ,'than' : '1' ,'that' : '1' ,'the' : '1' ,'their' : '1' ,'them' : '1' ,'then' : '1' ,'there' : '1' ,'therefore' : '1' ,'these' : '1' ,'they' : '1' ,'thing' : '1' ,'things' : '1' ,'think' : '1' ,'thinks' : '1' ,'this' : '1' ,'those' : '1' ,'though' : '1' ,'thought' : '1' ,'thoughts' : '1' ,'three' : '1' ,'through' : '1' ,'thus' : '1' ,'to' : '1' ,'today' : '1' ,'together' : '1' ,'too' : '1' ,'took' : '1' ,'toward' : '1' ,'turn' : '1' ,'turned' : '1' ,'turning' : '1' ,'turns' : '1' ,'two' : '1' ,'u' : '1' ,'under' : '1' ,'until' : '1' ,'up' : '1' ,'upon' : '1' ,'us' : '1' ,'use' : '1' ,'used' : '1' ,'uses' : '1' ,'v' : '1' ,'very' : '1' ,'w' : '1' ,'want' : '1' ,'wanted' : '1' ,'wanting' : '1' ,'wants' : '1' ,'was' : '1' ,'way' : '1' ,'ways' : '1' ,'we' : '1' ,'well' : '1' ,'wells' : '1' ,'went' : '1' ,'were' : '1' ,'what' : '1' ,'when' : '1' ,'where' : '1' ,'whether' : '1' ,'which' : '1' ,'while' : '1' ,'who' : '1' ,'whole' : '1' ,'whose' : '1' ,'why' : '1' ,'will' : '1' ,'with' : '1' ,'within' : '1' ,'without' : '1' ,'work' : '1' ,'worked' : '1' ,'working' : '1' ,'works' : '1' ,'would' : '1' ,'x' : '1' ,'y' : '1' ,'year' : '1' ,'years' : '1' ,'yet' : '1' ,'you' : '1' ,'young' : '1' ,'younger' : '1' ,'youngest' : '1' ,'your' : '1' ,'yours' : '1' ,'z' : '1','zero':'1'}      
      $("#q").bind('input', function(){
      //$("#q").keyup(function(){
        var query = $.trim($(this).val());
        var previousQuery = "";
        var splitQuery = query.split(" ");
        previousQuery = splitQuery.length > 1 ? query : previousQuery;
        query = splitQuery[splitQuery.length - 1];
        previousQuery = previousQuery.replace(query,"");
        
        $.ajax({
			    url:"http://localhost:8983/solr/myexample/suggest",
			    type:"GET",
			    data:{q:query,
                wt:"json",
                indent:"true"
                },
			    success:function(data){
				    suggestArray = constructAutoCompleteArray($.parseJSON(data), query, previousQuery);
            if (suggestArray.length > 0) {            
				      $(function() {
					      $("#q").autocomplete({
						      source : suggestArray,                  
					    });
				    });
           }
			    },
			    error: function(){				
			    }
		    });
        
        function constructAutoCompleteArray(data, query, previousQuery){
		      var suggestArray = [];
          var recordsFound = data.suggest.suggest[query].numFound;
          var suggestions = data.suggest.suggest[query].suggestions;          
          var stemmedHash = {};
          for (var i=0; i<recordsFound; i++){
            stemmedHash[stemmer(suggestions[i].term)] = suggestions[i].term;            
          }                    
          for (key in stemmedHash) {
           var suggestion = previousQuery + stemmedHash[key];
           if (!stopWords[suggestion.toLowerCase()]) {
            suggestArray.push(suggestion);
           }
          }                        
        return suggestArray;
        }
      });
    });    
    </script>            
  </body>
</html>
