package com.netease.backend.collector.rss.common.util;

public class JsInitUtil {
	public static String bigPhotoSingle() {
		return "<script>var args = {}; args.layout='photo-single';args.size = 'b';loadInit(args);</script>";
	}
	
	public static String photo() {
		return "<script>var args = {}; args.layout='photo';args.size = 'b';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String news() {
		return "<script>var args = {}; args.layout='news';args.size = 'b';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String video() {
		return "<script>var args = {}; args.layout='video';args.size = 'b';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String middlePhoto() {
		return "<script>var args = {}; args.layout='photo';args.size = 'm';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String middleNews() {
		return "<script>var args = {}; args.layout='news';args.size = 'm';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String middleVideo() {
		return "<script>var args = {}; args.layout='video';args.size = 'm';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String smallPhoto() {
		return "<script>var args = {}; args.layout='photo';args.size = 's';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String smallNews() {
		return "<script>var args = {}; args.layout='news';args.size = 's';window.onload = function() {loadInit(args);};</script>";
	}
	
	public static String smallVideo() {
		return "<script>var args = {}; args.layout='video';args.size = 's';window.onload = function() {loadInit(args);};</script>";
	}
}
