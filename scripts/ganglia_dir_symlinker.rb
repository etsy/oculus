#!/usr/bin/env ruby

require 'choice'

Choice.options do
    header ""
    header "Specific options:"
  
    option :source_dir, :required => true do
        short '-s'
        long  '--source DIR'
        desc  'Ganglia rrd directory'
    end
    
    option :dest_dir, :required => true do
        short '-d'
        long  '--dest DIR'
        desc  'Dir to create formatted symlinks in for graphite'
    end
end

source_dir = Choice.choices[:source_dir]
dest_dir = Choice.choices[:dest_dir]

Dir.chdir(source_dir)
listing = Dir.glob('*').select {|f| File.directory? f}

listing.each do |l|
  Dir.chdir(source_dir)
  puts l
  puts "Making top level dir #{dest_dir}/#{l} if it doesn't exist"
  Dir.mkdir(File.join(dest_dir, l), 0777) unless File.exist?(File.join(dest_dir, l))
  puts "Creating symlinks under #{dest_dir}/#{l}"
  Dir.chdir("#{source_dir}/#{l}")
  sub_listing = Dir.glob('*').select {|f| File.directory? f}
  sub_listing.each do |sl|
     source_path = "#{source_dir}/#{l}/#{sl}"
     formatted_listing = sl.gsub(".","_")
     dest_link = "#{dest_dir}/#{l}/#{formatted_listing}"
     puts "Symlinking #{source_path} to #{dest_link} if it doesn't exist"
     File.symlink(source_path, dest_link) unless File.exist?(dest_link)
  end
  
end