
var someDat_01 = { values: [
            {a: 'Z', b: 28},
            {a: 'Y', b: 55},
            {a: 'X', b: 43},
            {a: 'W', b: 91},
            {a: 'E', b: 81},
            {a: 'F', b: 53},
            {a: 'G', b: 19},
            {a: 'H', b: 87},
            {a: 'V', b: 52}
          ] };

var some02 = 'wow';


var barVl = {
    $schema: 'https://vega.github.io/schema/vega-lite/v3.json',
    description: 'Modified bar chart for someDat_01.',
    data: someDat_01,
    mark: 'bar',
    encoding: {
      x: {field: 'a', type: 'ordinal'},
      y: {field: 'b', type: 'quantitative'}
    }
};

var wthrVw = {
  "$schema": "https://vega.github.io/schema/vega-lite/v3.json",
  "description": "A dual axis chart, created by setting y's scale resolution to `\"independent\"`",
  "width": 400, "height": 300,
  "data": {
    "url": "axd_dat/copyOf_vegaWthrDat.csv",
    "format": {"type": "csv"}
  },
 "transform": [{"filter": "datum.location == \"New York\""}],    
//  "transform": [{"filter": "datum.location == \"Seattle\""}],
  "encoding": {
    "x": {
        "field": "date",
        "axis": {"format": "%b", "title": null},
        "type": "temporal",
        "timeUnit": "month"
    }
  },
  "layer": [
    {
      "mark": {"opacity": 0.3, "type": "area", "color": "#85C5A6"},
      "encoding": {
        "y": {
          "aggregate": "average",
          "field": "temp_max",
          "scale": {"domain": [0, 30]},
          "type": "quantitative",
          "axis": {"title": "Avg. Temperature (°C)", "titleColor": "#85C5A6"}
        },

        "y2": {
          "aggregate": "average",
          "field": "temp_min",
          "type": "quantitative"
        }
      }
    },
    {
      "mark": {"stroke": "#85A9C5", "type": "line", "interpolate": "monotone"},
      "encoding": {
        "y": {
          "aggregate": "average",
          "field": "precipitation",
          "type": "quantitative",
          "axis": {"title": "Precipitation (inches)", "titleColor":"#85A9C5"}
        }
      }
    }
  ],
  "resolve": {"scale": {"y": "independent"}}
}

