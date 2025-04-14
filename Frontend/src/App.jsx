import './App.css'
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from './pages/Home';
import Error from './pages/Error';
import Import from './pages/Import';
import Export from './pages/Export';

function App() {

  return (
    <>
    <BrowserRouter>
      <Routes>
        <Route path='/Home' element={<Home/>}/>
        <Route path='/Import' element={<Import/>}/>
        <Route path='/Export' element={<Export/>}/>
        <Route path='*' element={<Error/>}/>
      </Routes>
    </BrowserRouter>
    </>
  )
}

export default App;
