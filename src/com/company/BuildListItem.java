package com.company;

public class BuildListItem {

    public enum BuildListItemType {newZNI,newVersion,withoutChange,errCicleLinks,errBuildLinks,hasError,changeOnlyInstall,issueMismatch};

    private ReleaseItem releaseItem;
    private BuildListItemType Type;
    private String buildErrorLink;


    public BuildListItem()
    {
      releaseItem=new ReleaseItem("","","");
      Type=BuildListItemType.withoutChange;
      buildErrorLink="";
    };

    public BuildListItem(BuildListItemType type, ReleaseItem Item) {
        this.releaseItem=Item;
        Type = type;
        buildErrorLink="";
    }

    public ReleaseItem getItem() { return releaseItem; };

    public void setBuildError(String errZNI) {buildErrorLink=errZNI; }
    public String getBuildError() {return buildErrorLink; }


    public BuildListItemType getType() {
        return Type;
    }

    public void setType(BuildListItemType type, String errLink) {
        Type = type;

        if (buildErrorLink.isEmpty())
           buildErrorLink=buildErrorLink+errLink;
        else
            if (buildErrorLink.concat(errLink).isEmpty()) buildErrorLink=buildErrorLink+","+errLink;
    }
}
