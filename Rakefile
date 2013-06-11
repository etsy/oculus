require 'resque/tasks'
require File.join(File.dirname(__FILE__), 'workers/graphite_worker')
require File.join(File.dirname(__FILE__), 'workers/redis_worker')

namespace :resque do  
  task :setup

  desc "Restart running workers"
  task :restart_workers do
    Rake::Task['resque:stop_workers'].invoke
    Rake::Task['resque:start_workers'].invoke
  end
  
  desc "Quit running workers"
  task :stop_workers do
    stop_workers
  end
  
  desc "Start workers"
  task :start_workers do
    run_worker("process_graphite_namespace,process_redis_metrics", 22)
    run_worker("process_redis_metrics,process_graphite_namespace", 22)
  end
  
  
  
  def store_pids(pids, mode)
    pids_to_store = pids
    pids_to_store += read_pids if mode == :append

    # Make sure the pid file is writable.    
    File.open('/var/run/oculus/resque.pid', 'w') do |f|
      f <<  pids_to_store.join(',')
    end
  end

  def read_pids
    pid_file_path = "/var/run/oculus/resque.pid"
    return []  if ! File.exists?(pid_file_path)
    
    File.open(pid_file_path, 'r') do |f| 
      f.read 
    end.split(',').collect {|p| p.to_i }
  end

  def stop_workers
    pids = read_pids
    
    if pids.empty?
      puts "No workers to kill"
    else
      syscmd = "kill -s QUIT #{pids.join(' ')}"
      puts "$ #{syscmd}"
      `#{syscmd}`
      store_pids([], :write)
    end
  end
  
  # Start a worker with proper env vars and output redirection
  def run_worker(queue, count = 1)

    if !File.exists?("/var/run/oculus")
      puts "/var/run/oculus doesn't exist. Please create it."
      exit 1
    end

    if !File.readable?("/var/run/oculus")
      puts "/var/run/oculus isn't readable by this user. Please check permissions."
      exit 1
    end

    if !File.writable?("/var/run/oculus")
      puts "/var/run/oculus isn't writable by this user. Please check permissions."
      exit 1
    end

    if !File.exists?("/var/log/oculus")
      puts "/var/log/oculus doesn't exist. Please create it."
      exit 1
    end

    if !File.readable?("/var/log/oculus")
      puts "/var/log/oculus isn't readable by this user. Please check permissions."
      exit 1
    end

    if !File.writable?("/var/log/oculus")
      puts "/var/log/oculus isn't writable by this user. Please check permissions."
      exit 1
    end

    puts "Starting #{count} worker(s) with QUEUE: #{queue}"

    ##  make sure log/resque_err, log/resque_stdout are writable.
    ops = {:pgroup => true, :err => ["/var/log/oculus/resque_err", "a"], 
                            :out => ["/var/log/oculus/resque_stdout", "a"]}
    env_vars = {"REDIS_URL" => "http://oculusredis01:6379", "QUEUE" => queue.to_s, "TERM_CHILD" => "1"}

    pids = []
    count.times do
      ## Using Kernel.spawn and Process.detach because regular system() call would
      ## cause the processes to quit when capistrano finishes
      pid = spawn(env_vars, "rake resque:work", ops)
      Process.detach(pid)
      pids << pid
    end
    
    store_pids(pids, :append)
  end
end
