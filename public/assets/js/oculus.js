function page(page)
{
document.search.page.value=page
document.search.submit();
}

function query(query)
{
    document.search.page.value=1
    document.search.query.value=query
    document.search.submit();
}

function addfilter(filter)
{
    if (document.search.filters.value == "")
    {
        document.search.filters.value=filter
        document.search.submit();
    }
    else
    {
        document.search.filters.value=document.search.filters.value + "." + filter
        document.search.submit();
    }
}

function removefilter(filter)
{
    document.search.filters.value=document.search.filters.value.replace(filter,"").replace("..",".")
    if (document.search.filters.value.charAt(0) == ".")
    {
        document.search.filters.value=document.search.filters.value.slice(1)
    }
    document.search.submit();
}

function clearfilters()
{
    document.search.filters.value=""
    document.search.submit();
}

function addtocollection(metric_name)
{
    $.ajax({
        type: "GET",
        url: '/addtocollection?metric_name=' + metric_name,
        success: function(){
            rendercollection()
        },
        cache: false
    });
}

function rendercollection()
{
    $.ajax({
        url: '/tempcollection',
        success: function(data) {
            $('#tempcollection').html(data);
        }
    });
}

function deletecollection(name)
{
    $.ajax({
        url: '/deletecollection?name=' + name,
        success: function(data) {
            setTimeout(redirectToCollections, 1000)
        },
        error: function(data) {
            console.debug('FAIL!')
            setTimeout(redirectToCollections, 1000)

        }
    });
}

function redirectToCollections()
{
    document.location.href = '/collections';
}

function deletemetric(collection_name,metric_name)
{
    $.ajax({
        url: '/deletemetric?collection_name=' + collection_name + '&metric_name=' + metric_name,
        success: function(data) {
            document.location.href = '/collection/'+ collection_name + '?edit=true';
        },
        error: function(data) {
            console.debug('FAIL!')
            document.location.href = '/collection/'+ collection_name + '?edit=true';
        }
    });
}

function clearcollection()
{
    $.ajax({
        url: '/clearcollection',
        success: function(data) {
            $('#tempcollection').html(data);
        }
    });
}

function removefromcollection(metric_name)
{
    $.ajax({
        url: '/removefromcollection?metric_name=' + metric_name,
        success: function(data) {
            rendercollection()
        }
    });
}

function checkValue(searchType){
    if(searchType.val() == "FastDTW") {
        $('label[name="dtw_radius_label"]').show();
        $('label[icon="dtw_radius_icon"]').show();
        $('input[name="dtw_radius"]').show();
    }
    else{
        $('label[name="dtw_radius_label"]').hide();
        $('label[icon="dtw_radius_icon"]').hide();
        $('input[name="dtw_radius"]').hide();
    }
}

$(document).ready(function() {
    $('select').each(function() {
        var searchType = $(this);
        checkValue(searchType);
        searchType.on('change', function() {
            checkValue(searchType);
        });
    });
    rendercollection()
});