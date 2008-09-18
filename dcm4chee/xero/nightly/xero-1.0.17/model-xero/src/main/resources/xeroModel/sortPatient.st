/**
 * Handles a Sorting of columns in the StudyData results.
 */
function SortPatient(src) {
    this.order = new Array();
    this.layout = src;
};


SortPatient.prototype.sortColumn = function (col)   {
    var data = this.layout.search.patient;
  
    var compareAsc = function (item1, item2)  {
        if(item1[col] < item2[col]) return -1;
        if(item1[col]==item2[col]) return 0;
        return 1;
    }
      
    var compareDesc = function (item1, item2)  {
        if(item1[col] > item2[col]) return -1;
        if(item1[col] == item2[col]) return 0;
        return 1;
    }
      
    var compareAscFirstChild = function (item1, item2)  {
        if(item1.study[0][col] < item2.study[0][col]) return -1;
        if(item1.study[0][col] == item2.study[0][col]) return 0;
        return 1;
    }
      
    var compareDescFirstChild = function (item1, item2)  {
        if(item1.study[0][col] > item2.study[0][col]) return -1;
        if(item1.study[0][col] == item2.study[0][col]) return 0;
        return 1;
    }  
    
    var sortLevelOne = function (val, or) {
        if (or == "asc")   {
            val.sort(compareAsc);
        } else  {
            val.sort(compareDesc);
        }
    };  
    
    var sortLevelTwo = function (val, or) {
        if (or == "asc")    {
            for (i in val)  {
                val [i].study.sort(compareAsc);
            }
            val.sort(compareAscFirstChild);
        } else {
            for (i in val)  {
                val [i].study.sort(compareDesc);
            }
            val.sort(compareDescFirstChild);
        }
    };    
  
    // Check if it's Patient Name or Patient ID
    if (data[0][col])  {
        if (this.order[col] == undefined || this.order[col] == "desc")  {
            this.order[col]="asc";
        } else  {
            this.order[col]="desc";
        }
        sortLevelOne(data, this.order[col]);
    }
  
    // For Study Columns
    if (data[0].study[0][col])    {
        if (this.order[col] == undefined || this.order[col] == "desc")  {
            this.order[col]="asc";
        } else  {
            this.order[col]="desc";
        }
        sortLevelTwo(data, this.order[col]);
    }    
};