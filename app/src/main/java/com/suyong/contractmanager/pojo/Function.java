package com.suyong.contractmanager.pojo;

public class Function {
    private String name;
    private int ImageId;

    public Function(String name, int imageId) {
        this.name = name;
        ImageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return ImageId;
    }
}
