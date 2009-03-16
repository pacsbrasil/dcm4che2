/**
 * Handles a Sorting of columns in the StudyData results.
 */
function SortPatient(src) {
    this.layout = src;
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
            var a = String(item1[col]).toLowerCase(); 
            var b = String(item2[col]).toLowerCase();  
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
            var a = String(item1[col]).toLowerCase(); 
            var b = String(item2[col]).toLowerCase();          
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
        if (isNaN(item1.study[0][col]) && isNaN(item2.study[0][col]))   {
            var a = String(item1.study[0][col]).toLowerCase(); 
            var b = String(item2.study[0][col]).toLowerCase();           
            if(a < b) return -1;
            if(a == b) return 0;
            return 1;
        } else  {
            return item1.study[0][col]-item2.study[0][col];
        }        
    }
      
    /**
     * Descending compare function for top level elements of the study result. 
     * comparison based on the first child.
     */            
    var compareDescFirstChild = function (item1, item2)  {
        if (isNaN(item1.study[0][col]) && isNaN(item2.study[0][col]))   {
            var a = String(item1.study[0][col]).toLowerCase(); 
            var b = String(item2.study[0][col]).toLowerCase();                       
            if(a > b) return -1;
            if(a == b) return 0;
            return 1;
        } else  {
            return item2.study[0][col]-item1.study[0][col];
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
    if (data[0][col])  {
        if (this[col] == undefined || this[col] == "desc")  {
            this[col]="asc";
            this.img[col] = "icon/up_arrow_32.png";
        } else  {
            this[col]="desc";
            this.img[col] = "icon/down_arrow_32.png";
        }
        sortLevelOne(data, this[col]);
        console.info("%s IMAGE %s",col, SortPatient.prototype.img[col]);
    }
  
    // For Study Columns
    if (data[0].study[0][col])    {
        if (this[col] == undefined || this[col] == "desc")  {
            this[col]="asc";
            this.img[col] = "icon/up_arrow_32.png";
        } else  {
            this[col]="desc";
            this.img[col] = "icon/down_arrow_32.png";
        }
        sortLevelTwo(data, this[col]);
        console.info("%s IMAGE %s",col,SortPatient.prototype.img[col]);
    }    
};


