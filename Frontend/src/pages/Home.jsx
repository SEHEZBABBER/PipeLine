import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Button,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';

function Home() {
const navigate = useNavigate();
const [source, setSource] = useState('');
const [destination, setDestination] = useState('');
useEffect(() => {
    if (source === 'clickhouse') {
      setDestination('flatfile');
    } else if (source === 'flatfile') {
      setDestination('clickhouse');
    }
  }, [source]);

  useEffect(() => {
    // If destination changes, automatically update source if needed
    if (destination === 'clickhouse' && source !== 'flatfile') {
      setSource('flatfile');
    } else if (destination === 'flatfile' && source !== 'clickhouse') {
      setSource('clickhouse');
    }
  }, [destination]);
  function handleSubmit(e){
    e.preventDefault();
    if(source === '' || destination === '')alert("Please Select Something");
    else if(source === destination)alert("you have selected wrong things");
    else if(source === 'clickhouse' && destination === 'flatfile')navigate('/Import');
    else if(source === 'flatfile' && destination === 'clickhouse')navigate('/Export');
  }
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      height="100vh"
      width="100vw"
    >
      <Typography variant="h4" gutterBottom>
        Select The Operations That You Wanna Perform
      </Typography>

      <Box
        component="form"
        sx={{
          mt: 5,
          height: '40vh',
          width: '40vw',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 4,
        }}
        onSubmit={(e)=>handleSubmit(e)}
      >
        {/* Source Dropdown */}
        <FormControl fullWidth>
          <InputLabel id="source-label">Source</InputLabel>
          <Select
            labelId="source-label"
            id="source"
            value={source}
            label="Source"
            onChange={(e) => setSource(e.target.value)}
          >
            <MenuItem value="clickhouse">ClickHouse</MenuItem>
            <MenuItem value="flatfile">FlatFile</MenuItem>
          </Select>
        </FormControl>

        {/* Destination Dropdown */}
        <FormControl fullWidth>
          <InputLabel id="destination-label">Destination</InputLabel>
          <Select
            labelId="destination-label"
            id="destination"
            value={destination}
            label="Destination"
            onChange={(e) => setDestination(e.target.value)}
          >
            <MenuItem value="clickhouse">ClickHouse</MenuItem>
            <MenuItem value="flatfile">FlatFile</MenuItem>
          </Select>
        </FormControl>

        {/* Submit Button */}
        <Button variant="contained" color="error" onClick={handleSubmit}>
          {source === 'clickhouse' && destination === 'flatfile' ? (<span>Import Data</span>) : (<span>Export Data</span>)}
        </Button>
      </Box>
    </Box>
  );
}

export default Home;
