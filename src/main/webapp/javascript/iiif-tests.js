$(document).ready(
    function() {
      var url = purl();
      var protocol = url.attr('protocol'); /* Not currently using, but we will */
      var port = url.attr('port');
      var service = url.attr('host');

      if (port) {
        service += (':' + port);
      }

      /* resets the default server value to this machine if it's different */
      if ($('#djserver option:first').text() != service) {
        $option = $('<option></option>').attr('value', service).text(service);
        $('#djserver').append($option);
      }

      /* sets width to correct size for each select value */
      $('select').change(function() {
        var selected = -1;
        var opts = [];

        $(this).find("option").each(function(index) {
          if ($(this).is(':selected')) {
            selected = index;
          } else {
            $(this).remove();
          }

          opts.push($(this).val());
        });

        $(this).css('width', 'auto');
        width = parseInt($(this).css('width'));
        $(this).css('width', width);
        $(this).find("option:selected").remove();

        for ( var index = 0; index < opts.length; index++) {
          var value = opts[index];
          var option = $('<option></option>').attr('value', value).text(value);

          $(this).append(option);

          if (index == selected) {
            $(this).find('option:last').prop('selected', true);
          }
        }
      });

      /* sets the default select value */
      function setDefaultSelectValue(idName, idValue) {
        console.log(idValue);
        $('#' + idName + ' option').filter(function(index) {
          if ($(this).text() === idValue) {
            $(this).prop('selected', true);
            return true;
          } else {
            return false;
          }
        }).trigger('change');

        // account for the first load's different width (?)
        var width = parseInt($('#' + idName).css('width'));
        var fontSizeHalved = parseInt($('#' + idName).css('font-size')) / 2;
        $('#' + idName).css('width', width + fontSizeHalved);
      }

      if ($('#djserver option:first').text() != service) {
        setDefaultSelectValue('djserver', $('#djserver > option:last').val());
      }

      setDefaultSelectValue('djregion', 'full');
      setDefaultSelectValue('djsize', 'full');
      setDefaultSelectValue('djrotation', '0');

      // Changes the source of the image frame to a new IIIF request
      function refreshFrame() {
        var protocol = $('#djprotocol').val();
        var server = $('#djserver').val();
        var prefix = $('#djprefix').val();
        var id = $('#djidentifier').val();
        var region = $('#djregion').val();
        var size = $('#djsize').val();
        var rotation = $('#djrotation').val();
        var quality = $('#djquality').val();
        var format = $('#djformat').val();
        var request = protocol + '://' + server + '/' + prefix + '/' + id + '/'
            + region + '/' + size + '/' + rotation + '/' + quality + '.'
            + format;

        console.log('[DEBUG] IIIF request: ' + request);

        $('#iiif-image').attr('src', request);
      }

      /* changes the image when the 'refresh image' button is clicked */
      $('#refresh-image').click(function() {
        refreshFrame();
      });

      /* intercepts the 'enter' key so it can also be used to change the image */
      $(document).keypress(function(event) {
        if (event.which == 13) {
          refreshFrame();
          event.preventDefault();
          event.stopPropagation();
          return false;
        }
      });
    });