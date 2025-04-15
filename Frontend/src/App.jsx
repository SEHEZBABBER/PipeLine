import './App.css'
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from './pages/Home';
import Error from './pages/Error';
import Import from './pages/Import';
import Export from './pages/Export';
import SelectTabels from './pages/selectTabels';
import Columns from './pages/Columns';
import Preview from './pages/Preview';


function App() {
  return (
    <>
    <BrowserRouter>
      <Routes>
        <Route path='/Home' element={<Home/>}/>
        <Route path='/Import' element={<Import/>}/>
        <Route path='/Export' element={<Export/>}/>
        <Route path='/selectTabels' element={<SelectTabels/>}/>
        <Route path='/allcolumns' element={<Columns/>}/>
        <Route path='/preview' element={<Preview/>}/>
        <Route path='*' element={<Error/>}/>
      </Routes>
    </BrowserRouter>
    </>
  )
}

export default App;




