var fs = require('fs');
fs.readdir(__dirname+'/views/', function(err, dir_data)
{
	if(err)
		console.log(err);
	else
	{
		if(dir_data instanceof Array)
		{
			dir_data.forEach(function(file)
			{
				console.log('processing file: [%s]', file);
				let new_file = "";
				var lineReader = require('readline').createInterface(
				{
					input: require('fs').createReadStream(__dirname+'/views/'+file)
				});
				lineReader.on('line', function (line)
				{
					if(line.toString().includes("images"))
					{
						console.log('match, file [%s]: %s', file, line);
					}
					/*if(line.toString().includes("@../styles/home.css"))
					{
						new_file+='\r\n'+line.toString().replace("@../styles/home.css", "@../styles/home.css");
					} else if(line.toString().includes("@../styles/tabs.css"))
					{
						new_file+='\r\n'+line.toString().replace("@../styles/tabs.css", "@../styles/tabs.css");
					} else new_file+='\r\n'+line.toString();*/
					//new_file = new_file.replace(' ', '\u0020');
				});
				
				/*lineReader.on('close', function ()
				{
					fs.writeFile(__dirname+'/views/'+file, new_file, function(err)
					{
						if(err)
						{
							console.log(err);
							return;
						}
						console.log('done, new [%s]: %s', file, new_file);
					});
				});*/
			});
		}
	}});