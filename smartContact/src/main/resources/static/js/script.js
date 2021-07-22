console.log("start");

const toggleSidebar = () => {

    if($(".sidebar").is(":visible")) {
        // true
        //bnd krna h

        $(".sidebar").css("display", "none");
        $(".content").css("margin-left","0%");
    }
    else {
        //dikhana h
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left","20%");
    }
};