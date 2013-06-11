require 'rubygems'
require 'bundler'
Bundler.setup
require 'sinatra'
disable :run
require './oculusweb'
run Oculusweb