"use strict";
angular.module("app.views")
.controller('ruleCheckCtrl',['$scope','CommonService',function($scope,CommonService){
    $('body').niceScroll({cursorcolor:"#337ab7"});
    $scope.list = [];
    $scope.getList = function(num){
        CommonService.pagination({
            url : '/activityRule/findByPage.api',
            data :{
                pageNo : num || "1",
                name : $scope.ruleName
            }
        }).then(function(data){
            $scope.rules = data.item;
            $scope.page = data.page;
        });
    }
    $scope.getList();

    $scope.toRuleAdd = function(){
      localStorage.removeItem("manages");
       tabOpenParent({name:'ruleCheckDetail_add',url:'ruleCheckDetail.html',displayName:'新增规则'});
    };
    $scope.updateRule = function(id){
      localStorage.setItem("manages", id);
       tabOpenParent({name:'ruleCheckDetail_update',url:'ruleCheckDetail.html',displayName:'更新规则'});
    };

    document.onkeydown=function(event){
        var e = event || window.event || arguments.callee.caller.arguments[0];          
                 if(e && e.keyCode==13){ // enter 键
                   $scope.getList();
               }
           };    


}]);
