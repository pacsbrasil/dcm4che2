/**
 * Handles a Sorting of columns in the StudyData results.
 */
function SortPatient(src) {
    this.layout = src;
};

/**
 * Gets the given item as a lower-case string.  If the given item
 * is null or undefined, them empty string is returned.
 */
SortPatient.getItemString = function(item)
{
    if(item)
    {
        return String(item).toLowerCase();
    }
    else
    {
        return "";
    }
};

SortPatient.prototype.img = new Object();

/**
 * Sort study result column
 */
SortPatient.prototype.sortColumn = function (col)   {
    var data = this.layout.search.patient;
    this.img = [];
    /**
     * Ascending compare function for top level elements of the study result
     */
    var compareAsc = function (item1, item2)  {
        if (isNaN(item1) && isNaN(item2))   {
            var a = SortPatient.getItemString(item1[col.sort]); 
            var b = SortPatient.getItemString(item2[col.sort]);  
            if(a < b) return -1;
            if(a==b) return 0;
            return 1;
        } else  {
            return item1-item2;
        }
    }
      
    /**
     * Descending compare function for top level elements of the study result
     */      
    var compareDesc = function (item1, item2)  {
        if (isNaN(item1) && isNaN(item2))   {
            var a = SortPatient.getItemString(item1[col.sort]); 
            var b = SortPatient.getItemString(item2[col.sort]);  
            if(a > b) return -1;
            if(a == b) return 0;
            return 1;
        } else  {
            return item2-item1;
        }
    }
      
    /**
     * Ascending compare function for top level elements of the study result. 
     * comparison based on the first child.
     */      
    var compareAscFirstChild = function (item1, item2)  {
        if (isNaN(item1.study[0][col.sort]) && isNaN(item2.study[0][col.sort]))   {
            var a = SortPatient.getItemString(item1.study[0][col.sort]); 
            var b = SortPatient.getItemString(item2.study[0][col.sort]);           
            if(a < b) return -1;
            if(a == b) return 0;
            return 1;
        } else  {
            return item1.study[0][col.sort]-item2.study[0][col.sort];
        }        
    }
      
    /**
     * Descending compare function for top level elements of the study result. 
     * comparison based on the first child.
     */            
    var compareDescFirstChild = function (item1, item2)  {
        if (isNaN(item1.study[0][col.sort]) && isNaN(item2.study[0][col.sort]))   {
            var a = SortPatient.getItemString(item1.study[0][col.sort]); 
            var b = SortPatient.getItemString(item2.study[0][col.sort]);           
            if(a > b) return -1;
            if(a == b) return 0;
            return 1;
        } else  {
            return item2.study[0][col.sort]-item1.study[0][col.sort];
        }
    }  
    
    /**
     * Sort the columns that has the level one study elements   
     */    
    var sortLevelOne = function (val, or) {
        if (or == "asc")   {
            val.sort(compareAsc);
        } else  {
            val.sort(compareDesc);
        }
    };  
    
    /**
     * Sort the columns that has the level two study elements   
     */    
    var sortLevelTwo = function (val, or) {
        if (or == "asc")    {
            for (i in val)  {
                val [i].study.sort(compareAsc);
                for (j in val[i].study) {
                    val[i].study [j].xmlFirst=false;
                }
                val[i].study [0].xmlFirst=true;
            }
            val.sort(compareAscFirstChild);
        } else {
            for (i in val)  {
                val [i].study.sort(compareDesc);
                for (j in val[i].study) {
                    val[i].study [j].xmlFirst=false;
                }
                val[i].study [0].xmlFirst=true;
            }
            val.sort(compareDescFirstChild);
        }
    };    
  
    // Check if it's Patient Name or Patient ID
    if (col.level == "one")  {
        if (this[col.sort] == undefined || this[col.sort] == "desc")  {
            this[col.sort]="asc";
            this.img[col.sort] = "icon/up_arrow_32.png";
        } else  {
            this[col.sort]="desc";
            this.img[col.sort] = "icon/down_arrow_32.png";
        }
        sortLevelOne(data, this[col.sort]);
        console.info("%s IMAGE %s",col.sort, SortPatient.prototype.img[col.sort]);
    }
  
    // For Study columns
    if (col.level == "two")    {
        if (this[col.sort] == undefined || this[col.sort] == "desc")  {
            this[col.sort]="asc";
            this.img[col.sort] = "icon/up_arrow_32.png";
        } else  {
            this[col.sort]="desc";
            this.img[col.sort] = "icon/down_arrow_32.png";
        }
        sortLevelTwo(data, this[col.sort]);
        console.info("%s IMAGE %s",col.sort,SortPatient.prototype.img[col.sort]);
    }    
};


